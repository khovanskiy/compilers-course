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
    public void evaluate(final VMProgram vmProgram, final Map<Pointer, Symbol> symbols) {
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
    public void visitProgram(VMProgram vmProgram, EvaluatorContext context) {

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
    public void visitDup(VM.Dup command, EvaluatorContext context) {
        context.getStack().push(context.getStack().peek());
    }

    @Override
    public void visitIStore(VM.IStore command, EvaluatorContext context) {
        final VariablePointer pointer = new VariablePointer(getVariableName(command.getName()));
        final Symbol<Object> symbol = context.getStack().pop();
        assert Integer.class.isInstance(symbol.getValue());
        context.put(pointer, symbol);
    }

    @Override
    public void visitAStore(VM.AStore command, EvaluatorContext context) {
        final VariablePointer pointer = new VariablePointer(getVariableName(command.getName()));
        final Symbol<Object> symbol = context.getStack().pop();
        assert List.class.isInstance(symbol.getValue());
        context.put(pointer, symbol);
    }

    private String getVariableName(int index) {
        return "v" + index;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visitIAStore(VM.IAStore command, EvaluatorContext context) {
        final Symbol<Object> valueSymbol = context.getStack().pop();
        final Symbol<Object> indexSymbol = context.getStack().pop();
        final Symbol<Object> arraySymbol = context.getStack().pop();
        assert List.class.isInstance(arraySymbol.getValue());
        final List array = (List) arraySymbol.getValue();
        assert Integer.class.isInstance(indexSymbol.getValue()) : "Array index class is not integer: " + indexSymbol.getValue().getClass();
        final int index = (int) indexSymbol.getValue();
        assert Integer.class.isInstance(valueSymbol.getValue());
        array.set(index, valueSymbol.getValue());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visitAAStore(VM.AAStore command, EvaluatorContext context) {
        final Symbol<Object> valueSymbol = context.getStack().pop();
        final Symbol<Object> indexSymbol = context.getStack().pop();
        final Symbol<Object> arraySymbol = context.getStack().pop();
        assert List.class.isInstance(arraySymbol.getValue());
        final List array = (List) arraySymbol.getValue();
        assert Integer.class.isInstance(indexSymbol.getValue()) : "Array index class is not integer: " + indexSymbol.getValue().getClass();
        final int index = (int) indexSymbol.getValue();
        assert List.class.isInstance(valueSymbol.getValue());
        array.set(index, valueSymbol.getValue());
    }

    @Override
    public void visitILoad(VM.ILoad command, EvaluatorContext context) {
        final VariablePointer pointer = new VariablePointer(getVariableName(command.getName()));
        final Symbol<Object> symbol = context.get(pointer, Object.class);
        if (symbol == null) {
            throw new IllegalStateException(String.format("Variable \"%s\" is not found", command.getName()));
        }
        assert Integer.class.isInstance(symbol.getValue());
        context.getStack().add(symbol);
    }

    @Override
    public void visitALoad(VM.ALoad command, EvaluatorContext context) {
        final VariablePointer pointer = new VariablePointer(getVariableName(command.getName()));
        final Symbol<Object> symbol = context.get(pointer, Object.class);
        if (symbol == null) {
            throw new IllegalStateException(String.format("Variable \"%s\" is not found", command.getName()));
        }
        assert List.class.isInstance(symbol.getValue());
        context.getStack().add(symbol);
    }

    @Override
    public void visitIALoad(VM.IALoad command, EvaluatorContext context) {
        final Symbol<Object> indexSymbol = context.getStack().pop();
        final Symbol<Object> arraySymbol = context.getStack().pop();
        assert List.class.isInstance(arraySymbol.getValue());
        final List array = (List) arraySymbol.getValue();
        assert Integer.class.isInstance(indexSymbol.getValue()) : "Array index class is not integer: " + indexSymbol.getValue().getClass();
        final int index = (int) indexSymbol.getValue();
        Object value = array.get(index);
        assert Integer.class.isInstance(value);
        final Symbol<Object> valueSymbol = new Symbol<>(value);
        context.getStack().push(valueSymbol);
    }

    @Override
    public void visitAALoad(VM.AALoad command, EvaluatorContext context) {
        final Symbol<Object> indexSymbol = context.getStack().pop();
        final Symbol<Object> arraySymbol = context.getStack().pop();
        assert List.class.isInstance(arraySymbol.getValue());
        final List array = (List) arraySymbol.getValue();
        assert Integer.class.isInstance(indexSymbol.getValue()) : "Array index class is not integer: " + indexSymbol.getValue().getClass();
        final int index = (int) indexSymbol.getValue();
        Object value = array.get(index);
        assert List.class.isInstance(value);
        final Symbol<Object> valueSymbol = new Symbol<>(value);
        context.getStack().push(valueSymbol);
    }

    @Override
    public void visitLabel(VM.Label command, EvaluatorContext context) {
        // do nothing
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visitBinOp(VM.BinOp command, EvaluatorContext context) {
        assert context.getStack().size() >= 2 : "Stack does not have 2 values at least: " + Objects.toString(context.getStack());
        final Symbol<Object> second = context.getStack().pop();
        final Symbol<Object> first = context.getStack().pop();
        final Object value = ASTEvaluator.evaluateBinaryExpression(command.getOperator(), first.getValue(), second.getValue());
        context.getStack().push(new Symbol<>(value));
    }

    @Override
    public void visitAConstNull(VM.AConstNull command, EvaluatorContext context) {
        context.getStack().push(new Symbol<>(new NullPointer()));
    }

    @Override
    public void visitIConst(VM.IConst command, EvaluatorContext context) {
        context.getStack().push(new Symbol<>(command.getValue()));
    }

    @Override
    public void visitInvokeStatic(VM.InvokeStatic command, EvaluatorContext context) {
        final Symbol<ExternalFunction> function = context.get(new FunctionPointer(command.getName()), ExternalFunction.class);
        if (function != null) {
            final Object[] args = new Object[command.getArgumentsCount()];
            for (int i = command.getArgumentsCount() - 1; i >= 0; --i) {
                if (context.getStack().isEmpty()) {
                    throw new IllegalStateException(String.format("Missing argument for function \"%s\" external invoke", command.getName()));
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
            List<String> names = new ArrayList<>(command.getArgumentsCount());
            for (int i = 0; i < command.getArgumentsCount(); ++i) {
                names.add(getVariableName(scope.nextName()));
            }
            for (int i = command.getArgumentsCount() - 1; i >= 0; --i) {
                final Symbol symbol = context.getStack().pop();
                scope.getData().put(new VariablePointer(names.get(i)), symbol);
            }
            context.getScopes().push(scope);
            context.getCallStack().push(nextPosition);
            context.gotoLabel(command.getName());
        }
    }

    @Override
    public void visitReturn(VM.Return command, EvaluatorContext context) {
        context.getScopes().pop();
        final Position position = context.getCallStack().pop();
        context.setPosition(position);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visitIReturn(VM.IReturn command, EvaluatorContext context) {
        context.getScopes().pop();
        Symbol<Integer> value = context.getStack().pop();
        final Position position = context.getCallStack().pop();
        context.setPosition(position);
        context.getStack().push(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visitAReturn(VM.AReturn command, EvaluatorContext context) {
        context.getScopes().pop();
        Symbol<Object> value = context.getStack().pop();
        final Position position = context.getCallStack().pop();
        context.setPosition(position);
        context.getStack().push(value);
    }

    @Override
    public void visitGoto(VM.Goto command, EvaluatorContext context) {
        context.gotoLabel(command.getLabel());
    }

    @Override
    public void visitIfTrue(VM.IfTrue command, EvaluatorContext context) {
        Symbol<Integer> value = context.getStack().pop();
        if (value.getValue() != 0) {
            context.gotoLabel(command.getLabel());
        }
    }

    @Override
    public void visitIfFalse(VM.IfFalse command, EvaluatorContext context) {
        Symbol<Integer> value = context.getStack().pop();
        if (value.getValue() == 0) {
            context.gotoLabel(command.getLabel());
        }
    }

    @Override
    public void visitNewArray(VM.NewArray command, EvaluatorContext context) {
        Symbol<Integer> size = context.getStack().pop();
        List<Object> array = new ArrayList<>(size.getValue());
        for (int i = 0; i < size.getValue(); ++i) {
            array.add(null);
        }
        context.getStack().push(new Symbol<>(array));
    }

    @Override
    public void visitUnknown(VM command, EvaluatorContext context) {
        throw new UnsupportedOperationException(command.toString());
    }
}
