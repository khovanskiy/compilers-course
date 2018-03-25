package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.visitor.AbstractASTVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Evaluator extends AbstractASTVisitor<EvaluatorContext> {
    @Override
    public void visitCompoundStatement(AST.CompoundStatement compoundStatement, EvaluatorContext context) throws Exception {
        for (AST.SingleStatement singleStatement : compoundStatement.getStatements()) {
            visitSingleStatement(singleStatement, context);
            if (context.isShouldReturn()) {
                return;
            }
        }
    }

    @Override
    public void visitFunctionDefinition(AST.FunctionDefinition functionDefinition, EvaluatorContext evaluatorContext) {
        final FunctionPointer pointer = new FunctionPointer(functionDefinition.getName());
        final Symbol<UserFunction> newSymbol = new Symbol<>(new UserFunction(functionDefinition));
        evaluatorContext.update(pointer, oldSymbol -> {
            if (oldSymbol != null) {
                throw new IllegalStateException("Function is already defined");
            }
            return newSymbol;
        });
    }

    @Override
    public void visitVariableDefinition(AST.VariableDefinition variableDefinition, EvaluatorContext evaluatorContext) {
        // do nothing
    }

    public static int evaluateBinaryExpression(String operator, Object lo, Object ro) {
        int result;
        switch (operator) {
            case "*":
                result = (int) lo * (int) ro;
                break;
            case "/":
                result = (int) lo / (int) ro;
                break;
            case "%":
                result = (int) lo % (int) ro;
                break;
            case "+":
                result = (int) lo + (int) ro;
                break;
            case "-":
                result = (int) lo - (int) ro;
                break;
            case "&":
                result = (int) lo & (int) ro;
                break;
            case "^":
                result = (int) lo ^ (int) ro;
                break;
            case "|":
                result = (int) lo | (int) ro;
                break;
            case "&&":
                result = !Objects.equals(lo, 0) && !Objects.equals(ro, 0) ? 1 : 0;
                break;
            case "!!":
            case "||":
                result = !Objects.equals(lo, 0) || !Objects.equals(ro, 0) ? 1 : 0;
                break;
            case ">":
                result = (int) lo > (int) ro ? 1 : 0;
                break;
            case "<":
                result = (int) lo < (int) ro ? 1 : 0;
                break;
            case "<=":
                result = (int) lo <= (int) ro ? 1 : 0;
                break;
            case ">=":
                result = (int) lo >= (int) ro ? 1 : 0;
                break;
            case "==":
                result = Objects.equals(lo, ro) ? 1 : 0;
                break;
            case "!=":
                result = !Objects.equals(lo, ro) ? 1 : 0;
                break;
            default:
                throw new IllegalArgumentException("Unknown binary operator");
        }
        return result;
    }

    @Override
    public void visitVariableAccessForWrite(AST.VariableAccessExpression variableAccessExpression, EvaluatorContext context) {
        final VariablePointer pointer = new VariablePointer(variableAccessExpression.getName());
        context.setResult(new Symbol<>(pointer));
    }

    @Override
    public void visitArrayAccessForWrite(AST.ArrayAccessExpression arrayAccessExpression, EvaluatorContext context) throws Exception {
        visitExpression(arrayAccessExpression.getExpression(), context);
        final int index = context.getResult(Integer.class).getValue();
        visitMemoryAccessForWrite(arrayAccessExpression.getPointer(), context);
        final Pointer pointer = context.getResult(Pointer.class).getValue();
        context.setResult(new Symbol<>(new ArrayPointer(pointer, index)));
    }

    private void setValue(Pointer pointer, Function<Symbol, Symbol> callback, EvaluatorContext context) {
        if (pointer instanceof VariablePointer) {
            context.update(pointer, oldSymbol -> {
                final Symbol newSymbol = callback.apply(oldSymbol);
                if (oldSymbol != null && newSymbol != null) {
                    Class<?> oldClass = oldSymbol.getValue().getClass();
                    Class<?> newClass = newSymbol.getValue().getClass();
                    if (!oldClass.equals(newClass)) {
                        throw new IllegalStateException(String.format("Types of \"%s\" are not equal: %s != %s", pointer, oldClass, newClass));
                    }
                }
                return newSymbol;
            });
        } else if (pointer instanceof ArrayPointer) {
            ArrayPointer pointer1 = ((ArrayPointer) pointer);
            setValue(pointer1.getPointer(), oldSymbol -> {
                final Symbol newSymbol = callback.apply(oldSymbol);
                final List oldValue = List.class.cast(oldSymbol.getValue());
                oldValue.set(pointer1.getIndex(), newSymbol.getValue());
                return new Symbol<>(oldValue);
            }, context);
        }
    }

    @Override
    public void visitIfStatement(AST.IfStatement ifStatement, EvaluatorContext context) throws Exception {
        final List<AST.IfCase> cases = ifStatement.getCases();
        for (int i = 0; i < cases.size(); ++i) {
            final AST.IfCase ifCase = cases.get(i);
            if (ifCase.getCondition() == null) {
                // else
                assert i == cases.size() - 1;
                visitCompoundStatement(ifCase.getCompoundStatement(), context);
                return;
            }
            // if or elif
            visitExpression(ifCase.getCondition(), context);
            final boolean success = context.getResult(Integer.class).getValue() == 1;
            if (success) {
                visitCompoundStatement(ifCase.getCompoundStatement(), context);
                return;
            }
        }
    }

    @Override
    public void visitGotoStatement(AST.GotoStatement gotoStatement, EvaluatorContext evaluatorContext) {

    }

    @Override
    public void visitContinueStatement(AST.ContinueStatement continueStatement, EvaluatorContext evaluatorContext) {

    }

    @Override
    public void visitBreakStatement(AST.BreakStatement breakStatement, EvaluatorContext evaluatorContext) throws Exception {

    }

    @Override
    public void visitReturnStatement(AST.ReturnStatement returnStatement, EvaluatorContext context) throws Exception {
        visitExpression(returnStatement.getExpression(), context);
        context.setResult(context.getResult(Object.class));
        context.setShouldReturn(true);
    }

    @Override
    public void visitSkipStatement(AST.SkipStatement skipStatement, EvaluatorContext evaluatorContext) {
        // do nothing
    }

    @Override
    public void visitWhileStatement(AST.WhileStatement whileStatement, EvaluatorContext context) throws Exception {
        visitExpression(whileStatement.getCondition(), context);
        boolean loop = context.getResult(Integer.class).getValue() != 0;
        while (loop) {
            visitCompoundStatement(whileStatement.getCompoundStatement(), context);
            visitExpression(whileStatement.getCondition(), context);
            loop = context.getResult(Integer.class).getValue() != 0;
        }
    }

    @Override
    public void visitRepeatStatement(AST.RepeatStatement repeatStatement, EvaluatorContext context) throws Exception {
        boolean loop;
        do {
            visitCompoundStatement(repeatStatement.getCompoundStatement(), context);
            visitExpression(repeatStatement.getCondition(), context);
            loop = context.getResult(Integer.class).getValue() == 0;
        } while (loop);
    }

    @Override
    public void visitForStatement(AST.ForStatement forStatement, EvaluatorContext context) throws Exception {
        if (forStatement.getInit() != null) {
            visitAssignmentStatement(forStatement.getInit(), context);
        }
        boolean loop = true;
        if (forStatement.getCondition() != null) {
            visitExpression(forStatement.getCondition(), context);
            loop = context.getResult(Integer.class).getValue() != 0;
        }
        while (loop) {
            visitCompoundStatement(forStatement.getCompoundStatement(), context);
            if (forStatement.getLoop() != null) {
                visitAssignmentStatement(forStatement.getLoop(), context);
            }
            if (forStatement.getCondition() != null) {
                visitExpression(forStatement.getCondition(), context);
                loop = context.getResult(Integer.class).getValue() != 0;
            }
        }
    }

    @Override
    public void visitFunctionCall(AST.FunctionCall functionCall, EvaluatorContext context) throws Exception {
        final FunctionPointer pointer = new FunctionPointer(functionCall.getName());
        Symbol<MyFunction> symbol = context.get(pointer, MyFunction.class);
        if (symbol == null) {
            throw new IllegalStateException(String.format("Function \"%s\" is not defined", functionCall.getName()));
        }
        MyFunction function = symbol.getValue();
        if (function instanceof ExternalFunction) {
            final Object[] args = new Object[functionCall.getArguments().size()];
            for (int i = 0; i < functionCall.getArguments().size(); ++i) {
                final AST.Expression expression = functionCall.getArguments().get(i);
                visitExpression(expression, context);
                args[i] = context.getResult(Object.class).getValue();
            }
            final Object result = ((ExternalFunction) function).evaluate(args);
            context.setResult(new Symbol<>(result));
            return;
        }
        if (function instanceof UserFunction) {
            AST.FunctionDefinition definition = ((UserFunction) function).getDefinition();
            if (functionCall.getArguments().size() != definition.getVariables().size()) {
                throw new IllegalStateException();
            }
            EvaluatorContext newContext = new EvaluatorContext(context);
            for (int i = 0; i < functionCall.getArguments().size(); ++i) {
                final AST.VariableDefinition variable = definition.getVariables().get(i);
                final AST.Expression expression = functionCall.getArguments().get(i);
                visitExpression(expression, context);
                final VariablePointer variablePointer = new VariablePointer(variable.getName());
                newContext.put(variablePointer, context.getResult(Object.class));
            }
            visitCompoundStatement(definition.getCompoundStatement(), newContext);
            context.setResult(newContext.getResult(Object.class));
            context.setShouldReturn(false);
            return;
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitArrayCreation(AST.ArrayCreationExpression arrayCreationExpression, EvaluatorContext context) throws Exception {
        List<Object> arr = new ArrayList<>(arrayCreationExpression.getArguments().size());
        for (int i = 0; i < arrayCreationExpression.getArguments().size(); ++i) {
            visitExpression(arrayCreationExpression.getArguments().get(i), context);
            arr.add(context.getResult(Object.class).getValue());
        }
        context.setResult(new Symbol<>(arr));
    }

    @Override
    public void visitVariableAccess(AST.VariableAccessExpression variableAccessExpression, EvaluatorContext evaluatorContext) {
        // right-value case
        final VariablePointer pointer = new VariablePointer(variableAccessExpression.getName());
        final Symbol symbol = evaluatorContext.get(pointer, Object.class);
        evaluatorContext.setResult(symbol);
    }

    @Override
    public void visitArrayAccess(AST.ArrayAccessExpression arrayAccessExpression, EvaluatorContext context) throws Exception {
        // right-value case
        visitExpression(arrayAccessExpression.getExpression(), context);
        final int index = context.getResult(Integer.class).getValue();
        visitMemoryAccess(arrayAccessExpression.getPointer(), context);
        final List array = context.getResult(List.class).getValue();
        context.setResult(new Symbol<>(array.get(index)));
    }

//    public Symbol getValue(Pointer pointer, EvaluatorContext context) {
//        if (pointer instanceof VariablePointer) {
//            return context.get(pointer, Object.class);
//        } else if (pointer instanceof ArrayPointer) {
//            final ArrayPointer arrayPointer = (ArrayPointer) pointer;
//            Symbol symbol = getValue(arrayPointer.getPointer(), context);
//            int[] array = int[].class.cast(symbol.getValue());
//            int index = arrayPointer.getIndex();
//            return new Symbol<>(array[index]);
//        }
//        throw new IllegalStateException();
//    }

    @Override
    public void visitIntegerLiteral(AST.IntegerLiteral integerLiteral, EvaluatorContext context) {
        context.setResult(new Symbol<>(integerLiteral.getValue()));
    }

    @Override
    public void visitCharacterLiteral(AST.CharacterLiteral characterLiteral, EvaluatorContext context) throws Exception {
        final char character = characterLiteral.getValue();
        context.setResult(new Symbol<>((int) character));
    }

    @Override
    public void visitStringLiteral(AST.StringLiteral stringLiteral, EvaluatorContext context) {
        final String string = stringLiteral.getValue();
        final int length = string.length();
        final List<Integer> array = new ArrayList<>(length);
        for (int i = 0; i < length; ++i) {
            array.add((int) string.charAt(i));
        }
        context.setResult(new Symbol<>(array));
    }

    @Override
    public void visitNullLiteral(AST.NullLiteral nullLiteral, EvaluatorContext context) throws Exception {
        context.setResult(new Symbol<>(new NullPointer()));
    }

    @Override
    public void visitUnaryExpression(AST.UnaryExpression unaryExpression, EvaluatorContext evaluatorContext) throws Exception {

    }

    @Override
    public void visitAssignmentStatement(AST.AssignmentStatement assignmentStatement, EvaluatorContext context) throws Exception {
        AST.Expression expression = assignmentStatement.getExpression();
        visitExpression(expression, context);
        final Symbol newSymbol = context.getResult(Object.class);
        AST.MemoryAccessExpression memoryAccess = assignmentStatement.getMemoryAccess();
        if (memoryAccess instanceof AST.VariableAccessExpression) {
            visitVariableAccessForWrite((AST.VariableAccessExpression) memoryAccess, context);
            final VariablePointer pointer = context.getResult(VariablePointer.class).getValue();
            setValue(pointer, k -> newSymbol, context);
            return;
        }
        if (memoryAccess instanceof AST.ArrayAccessExpression) {
            visitArrayAccessForWrite((AST.ArrayAccessExpression) memoryAccess, context);
            final ArrayPointer pointer = context.getResult(ArrayPointer.class).getValue();
            setValue(pointer, k -> newSymbol, context);
            return;
        }
        throw new IllegalStateException("Unknown memory access type: " + memoryAccess.getClass());
    }

    @Override
    public void visitBinaryExpression(AST.BinaryExpression binaryExpression, EvaluatorContext context) throws Exception {
        visitExpression(binaryExpression.getLeft(), context);
        final Object lo = context.getResult(Object.class).getValue();
        visitExpression(binaryExpression.getRight(), context);
        final Object ro = context.getResult(Object.class).getValue();
        if (lo.getClass() != ro.getClass()) {
            throw new RuntimeException();
        }
        int result = evaluateBinaryExpression(binaryExpression.getOperator(), lo, ro);
        context.setResult(new Symbol<>(result));
    }
}
