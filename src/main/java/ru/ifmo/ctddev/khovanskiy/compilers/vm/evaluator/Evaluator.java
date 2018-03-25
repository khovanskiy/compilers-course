package ru.ifmo.ctddev.khovanskiy.compilers.vm.evaluator;

import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.*;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.visitor.AbstractVMVisitor;

import java.util.*;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class Evaluator extends AbstractVMVisitor<EvaluatorContext> {
    @Override
    public void visitProgram(VMProgram vmProgram, EvaluatorContext context) throws Exception {
        final List<VM> commands = vmProgram.getCommands();
        final Map<String, Integer> labels = new HashMap<>();
        for (int i = 0; i < commands.size(); i++) {
            VM vm = commands.get(i);
            if (vm instanceof VM.Label) {
                String name = ((VM.Label) vm).getName();
                int newLine = i;
                labels.compute(name, (key, oldLine) -> {
                    if (oldLine != null) {
                        throw new IllegalStateException(String.format("Label \"%s\" is duplicated at lines: %d and %d", name, oldLine, newLine));
                    }
                    return newLine;
                });
            }
        }
        context.setLabels(labels);
        context.gotoLabel("main");

        int position = context.getPosition();
        while (position < commands.size()) {
            VM command = commands.get(position);
            visitCommand(command, context);
            position = context.nextPosition();
        }
    }

    @Override
    public void visitDup(VM.Dup dup, EvaluatorContext context) {
        context.getStack().push(context.getStack().peek());
    }

    @Override
    public void visitIStore(VM.IStore store, EvaluatorContext context) throws Exception {
        final VariablePointer pointer = new VariablePointer(store.getName());
        final Symbol<Object> symbol = context.getStack().pop();
        context.put(pointer, symbol);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visitIAStore(VM.IAStore iaStore, EvaluatorContext context) {
        final Symbol<Object> valueSymbol = context.getStack().pop();
        final Symbol<Object> indexSymbol = context.getStack().pop();
        final Symbol<Object> arraySymbol = context.getStack().pop();
        assert List.class.isInstance(arraySymbol.getValue());
        final List array = (List) arraySymbol.getValue();
        assert Integer.class.isInstance(indexSymbol.getValue()) : "Array index class is not integer: " + indexSymbol.getValue().getClass();
        final int index = (int) indexSymbol.getValue();
        array.set(index, valueSymbol.getValue());
    }

    @Override
    public void visitILoad(VM.ILoad load, EvaluatorContext context) throws Exception {
        final VariablePointer pointer = new VariablePointer(load.getName());
        final Symbol<Object> symbol = context.get(pointer, Object.class);
        if (symbol == null) {
            throw new IllegalStateException(String.format("Variable \"%s\" is not found", load.getName()));
        }
        context.getStack().add(symbol);
    }

    @Override
    public void visitIALoad(VM.IALoad iaLoad, EvaluatorContext context) {
        final Symbol<Object> indexSymbol = context.getStack().pop();
        final Symbol<Object> arraySymbol = context.getStack().pop();
        assert List.class.isInstance(arraySymbol.getValue());
        final List array = (List) arraySymbol.getValue();
        assert Integer.class.isInstance(indexSymbol.getValue()) : "Array index class is not integer: " + indexSymbol.getValue().getClass();
        final int index = (int) indexSymbol.getValue();
        final Symbol<Object> valueSymbol = new Symbol<>(array.get(index));
        context.getStack().push(valueSymbol);
    }

    @Override
    public void visitLabel(VM.Label label, EvaluatorContext context) {
        // do nothing
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visitBinOp(VM.BinOp binOp, EvaluatorContext context) {
        assert context.getStack().size() >= 2 : "Stack does not have 2 values at least: " + Objects.toString(context.getStack());
        final Symbol<Object> second = context.getStack().pop();
        final Symbol<Object> first = context.getStack().pop();
        final Object value = ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.Evaluator.evaluateBinaryExpression(binOp.getOperator(), first.getValue(), second.getValue());
        context.getStack().push(new Symbol<>(value));
    }

    @Override
    public void visitAConstNull(VM.AConstNull aConstNull, EvaluatorContext context) {
        context.getStack().push(new Symbol<>(new NullPointer()));
    }

    @Override
    public void visitIConst(VM.IConst iConst, EvaluatorContext context) {
        context.getStack().push(new Symbol<>(iConst.getValue()));
    }

    @Override
    public void visitInvokeExternal(VM.InvokeExternal invokeExternal, EvaluatorContext context) throws Exception {
        final Symbol<ExternalFunction> function = context.get(new FunctionPointer(invokeExternal.getName()), ExternalFunction.class);
        if (function == null) {
            throw new IllegalStateException(String.format("The external executor for function \"%s\" is not found", invokeExternal.getName()));
        }
        final Object[] args = new Object[invokeExternal.getArgumentsCount()];
        for (int i = invokeExternal.getArgumentsCount() - 1; i >= 0; --i) {
            if (context.getStack().isEmpty()) {
                throw new IllegalStateException(String.format("Missing argument for function \"%s\" external invoke", invokeExternal.getName()));
            }
            args[i] = context.getStack().pop().getValue();
        }
        final Object value = function.getValue().evaluate(args);
        if (value != null) {
            context.getStack().push(new Symbol<>(value));
        }
    }

    @Override
    public void visitInvokeStatic(VM.InvokeStatic call, EvaluatorContext context) throws Exception {
        final Symbol<ExternalFunction> function = context.get(new FunctionPointer(call.getName()), ExternalFunction.class);
        if (function != null) {
            final Object[] args = new Object[call.getArgumentsCount()];
            for (int i = call.getArgumentsCount() - 1; i >= 0; --i) {
                if (context.getStack().isEmpty()) {
                    throw new IllegalStateException(String.format("Missing argument for function \"%s\" external invoke", call.getName()));
                }
                args[i] = context.getStack().pop().getValue();
            }
            final Object value = function.getValue().evaluate(args);
            if (value != null) {
                context.getStack().push(new Symbol<>(value));
            }
        } else {
            int position = context.getPosition();
            EvaluatorContext.Scope scope = new EvaluatorContext.Scope();
            List<String> names = new ArrayList<>(call.getArgumentsCount());
            for (int i = 0; i < call.getArgumentsCount(); ++i) {
                names.add(scope.nextName());
            }
            for (int i = call.getArgumentsCount() - 1; i >= 0; --i) {
                final Symbol symbol = context.getStack().pop();
                scope.getData().put(new VariablePointer(names.get(i)), symbol);
            }
            context.getScopes().push(scope);
            context.getCallStack().push(position);
            context.gotoLabel(call.getName());
        }
    }

    @Override
    public void visitReturn(VM.Return vmReturn, EvaluatorContext context) {
        context.getScopes().pop();
        int position = context.getCallStack().pop();
        context.setPosition(position);
    }

    @Override
    public void visitIReturn(VM.IReturn iReturn, EvaluatorContext context) {
        context.getScopes().pop();
        Symbol<Integer> value = context.getStack().pop();
        int position = context.getCallStack().pop();
        context.setPosition(position);
        context.getStack().push(value);
    }

    @Override
    public void visitGoto(VM.Goto vmGoto, EvaluatorContext context) {
        context.gotoLabel(vmGoto.getLabel());
    }

    @Override
    public void visitIfTrue(VM.IfTrue ifTrue, EvaluatorContext context) {
        Symbol<Integer> value = context.getStack().pop();
        if (value.getValue() != 0) {
            context.gotoLabel(ifTrue.getLabel());
        }
    }

    @Override
    public void visitIfFalse(VM.IfFalse ifFalse, EvaluatorContext context) {
        Symbol<Integer> value = context.getStack().pop();
        if (value.getValue() == 0) {
            context.gotoLabel(ifFalse.getLabel());
        }
    }

    @Override
    public void visitNewArray(VM.NewArray newArray, EvaluatorContext context) {
        Symbol<Integer> size = context.getStack().pop();
        List<Object> array = new ArrayList<>(size.getValue());
        for (int i = 0; i < size.getValue(); ++i) {
            array.add(null);
        }
        context.getStack().push(new Symbol<>(array));
    }

    @Override
    public void visitUnknown(VM vm, EvaluatorContext context) {
        throw new UnsupportedOperationException(vm.toString());
    }
}
