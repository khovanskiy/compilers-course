package ru.ifmo.ctddev.khovanskiy.compilers.vm.evaluator;

import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.ExternalFunction;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.Symbol;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.VariablePointer;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.visitor.AbstractVMVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void visitStore(VM.Store store, EvaluatorContext context) throws Exception {
        final VariablePointer pointer = new VariablePointer(store.getName());
        final Symbol<Object> symbol = context.getStack().pop();
        context.put(pointer, symbol);
    }

    @Override
    public void visitLoad(VM.Load load, EvaluatorContext context) throws Exception {
        final VariablePointer pointer = new VariablePointer(load.getName());
        final Symbol<Object> symbol = context.get(pointer, Object.class);
        if (symbol == null) {
            throw new IllegalStateException(String.format("Variable \"%s\" is not found", load.getName()));
        }
        context.getStack().add(symbol);
    }

    @Override
    public void visitLabel(VM.Label label, EvaluatorContext context) {
        // do nothing
    }

    @Override
    public void visitBinOp(VM.BinOp binOp, EvaluatorContext context) {
        final Symbol<Object> second = context.getStack().pop();
        final Symbol<Object> first = context.getStack().pop();
        final Object value = ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.Evaluator.evaluateBinaryExpression(binOp.getOperator(), first.getValue(), second.getValue());
        context.getStack().push(new Symbol<>(value));
    }

    @Override
    public void visitIConst(VM.IConst iConst, EvaluatorContext context) {
        context.getStack().add(new Symbol<>(iConst.getValue()));
    }

    @Override
    public void visitInvokeExternal(VM.InvokeExternal invokeExternal, EvaluatorContext context) throws Exception {
        final ExternalFunction function = context.getExternalFunctions().get(invokeExternal.getName());
        final Object[] args = new Object[invokeExternal.getArgumentsCount()];
        for (int i = 0; i < invokeExternal.getArgumentsCount(); ++i) {
            if (context.getStack().isEmpty()) {
                throw new IllegalStateException(String.format("Missing argument for function \"%s\" external invoke", invokeExternal.getName()));
            }
            args[i] = context.getStack().pop().getValue();
        }
        final Object value = function.evaluate(args);
        if (value != null) {
            context.getStack().push(new Symbol<>(value));
        }
    }

    @Override
    public void visitInvokeStatic(VM.InvokeStatic call, EvaluatorContext context) {
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
    public void visitUnknown(VM vm, EvaluatorContext context) {
        throw new UnsupportedOperationException(vm.toString());
    }
}
