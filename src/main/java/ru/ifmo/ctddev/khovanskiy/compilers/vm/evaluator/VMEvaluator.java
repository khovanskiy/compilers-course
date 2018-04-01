package ru.ifmo.ctddev.khovanskiy.compilers.vm.evaluator;

import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.*;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMFunction;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.visitor.AbstractVMVisitor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class VMEvaluator extends AbstractVMVisitor<EvaluatorContext> {
    public void evaluate(final VMProgram vmProgram, final Map<Pointer, Symbol> symbols) throws Exception {
        final List<VMFunction> functions = vmProgram.getFunctions();
        final Map<String, Position> labels = new HashMap<>();
        for (final VMFunction function : functions) {
            registerLabel(labels, function, function.getName(), 0);
            final List<VM> commands = function.getCommands();
            for (int i = 0; i < commands.size(); i++) {
                VM vm = commands.get(i);
                if (vm instanceof VM.Label) {
                    String name = ((VM.Label) vm).getName();
                    registerLabel(labels, function, name, i);
                }
            }
        }
        final EvaluatorContext context = new EvaluatorContext(symbols);
        context.setLabels(labels);
        visitProgram(vmProgram, context);
    }

    protected void registerLabel(final Map<String, Position> labels, VMFunction function, String name, int newLine) {
        labels.compute(name, (key, oldPosition) -> {
            final Position newPosition = new Position(function.getName(), newLine);
            if (oldPosition != null) {
                throw new IllegalStateException(String.format("Label \"%s\" is duplicated at lines: %d and %d", name, oldPosition.getLineNumber(), newLine));
            }
            return newPosition;
        });
    }

    @Override
    public void visitProgram(VMProgram vmProgram, EvaluatorContext context) throws Exception {

        Map<String, VMFunction> vmFunctionMap = vmProgram.getFunctions().stream()
                .collect(Collectors.toMap(VMFunction::getName, Function.identity(), (u, v) -> {
                    throw new IllegalStateException();
                }));

        context.gotoLabel("main");
        while (true) {
            final VMFunction function = vmFunctionMap.get(context.getPosition().getFunctionName());
            final List<VM> commands = function.getCommands();
            if (context.getPosition().getLineNumber() >= commands.size()) {
                break;
            }
            VM command = commands.get(context.getPosition().getLineNumber());
            visitCommand(command, context);
            if (!VM.AbstractInvoke.class.isInstance(command) && !VM.AbstractReturn.class.isInstance(command)) {
                context.nextPosition();
            }
        }
    }

    @Override
    public void visitDup(VM.Dup dup, EvaluatorContext context) {
        context.getStack().push(context.getStack().peek());
    }

    @Override
    public void visitIStore(VM.IStore store, EvaluatorContext context) throws Exception {
        final VariablePointer pointer = new VariablePointer(getVariableName(store.getName()));
        final Symbol<Object> symbol = context.getStack().pop();
        context.put(pointer, symbol);
    }

    private String getVariableName(int index) {
        return "v" + index;
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
        final VariablePointer pointer = new VariablePointer(getVariableName(load.getName()));
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
            context.nextPosition();
        } else {
            final Position position = context.getPosition();
            final Position nextPosition = new Position(position.getFunctionName(), position.getLineNumber() + 1);
            EvaluatorContext.Scope scope = new EvaluatorContext.Scope();
            List<String> names = new ArrayList<>(call.getArgumentsCount());
            for (int i = 0; i < call.getArgumentsCount(); ++i) {
                names.add(getVariableName(scope.nextName()));
            }
            for (int i = call.getArgumentsCount() - 1; i >= 0; --i) {
                final Symbol symbol = context.getStack().pop();
                scope.getData().put(new VariablePointer(names.get(i)), symbol);
            }
            context.getScopes().push(scope);
            context.getCallStack().push(nextPosition);
            context.gotoLabel(call.getName());
        }
    }

    @Override
    public void visitReturn(VM.Return vmReturn, EvaluatorContext context) {
        context.getScopes().pop();
        final Position position = context.getCallStack().pop();
        context.setPosition(position);
    }

    @Override
    public void visitIReturn(VM.IReturn iReturn, EvaluatorContext context) {
        context.getScopes().pop();
        Symbol<Integer> value = context.getStack().pop();
        final Position position = context.getCallStack().pop();
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
