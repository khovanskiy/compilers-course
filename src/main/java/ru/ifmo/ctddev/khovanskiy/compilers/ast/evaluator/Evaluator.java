package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.visitor.AbstractASTVisitor;

import java.util.List;
import java.util.Objects;

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
    public void visitVariableDefinition(AST.VariableDefinition variableDefinition, EvaluatorContext evaluatorContext) throws Exception {

    }

    @Override
    public void visitAssignmentStatement(AST.AssignmentStatement assignmentStatement, EvaluatorContext context) throws Exception {
        AST.Expression expression = assignmentStatement.getExpression();
        visitExpression(expression, context);
        final Symbol newSymbol = context.getResult(Object.class);
        AST.MemoryAccessExpression memoryAccess = assignmentStatement.getMemoryAccess();
        if (memoryAccess instanceof AST.VariableAccessExpression) {
            AST.VariableAccessExpression a = (AST.VariableAccessExpression) memoryAccess;
            final VariablePointer pointer = new VariablePointer(a.getName());
            context.update(pointer, oldSymbol -> {
                if (oldSymbol != null && newSymbol != null && !oldSymbol.getValue().getClass().equals(newSymbol.getValue().getClass())) {
                    throw new IllegalStateException();
                }
                return newSymbol;
            });
            return;
        }
//        if (memoryAccess instanceof AST.ArrayAccessExpression) {
//            c
//        }
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
    public void visitArrayCreation(AST.ArrayCreationExpression arrayCreationExpression, EvaluatorContext evaluatorContext) throws Exception {

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
        visitExpression(arrayAccessExpression.getExpressions(), context);
        final Symbol<Integer> index = context.getResult(Integer.class);
        visitMemoryAccess(arrayAccessExpression.getPointer(), context);
        //final context.getResult(int[].class).getValue()[index.getValue()];
        final ArrayPointer pointer = new ArrayPointer();
    }

    @Override
    public void visitIntegerLiteral(AST.IntegerLiteral integerLiteral, EvaluatorContext context) {
        context.setResult(new Symbol<>(integerLiteral.getValue()));
    }

    @Override
    public void visitCharacterLiteral(AST.CharacterLiteral characterLiteral, EvaluatorContext context) throws Exception {
        context.setResult(new Symbol<>(characterLiteral.getValue()));
    }

    @Override
    public void visitStringLiteral(AST.StringLiteral stringLiteral, EvaluatorContext context) {
        context.setResult(new Symbol<>(stringLiteral.getValue().toCharArray()));
    }

    @Override
    public void visitNullLiteral(AST.NullLiteral nullLiteral, EvaluatorContext evaluatorContext) throws Exception {

    }

    @Override
    public void visitUnaryExpression(AST.UnaryExpression unaryExpression, EvaluatorContext evaluatorContext) throws Exception {

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
        int result;
        switch (binaryExpression.getOperator()) {
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
                result = Objects.equals(lo, 1) && Objects.equals(ro, 1) ? 1 : 0;
                break;
            case "||":
                result = Objects.equals(lo, 1) || Objects.equals(ro, 1) ? 1 : 0;
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
                throw new IllegalArgumentException("Unknown binary operation");
        }
        context.setResult(new Symbol<>(result));
    }
}
