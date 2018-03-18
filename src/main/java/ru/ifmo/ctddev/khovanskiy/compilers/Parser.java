package ru.ifmo.ctddev.khovanskiy.compilers;

import lombok.extern.slf4j.Slf4j;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;
import ru.ifmo.ctddev.khovanskiy.compilers.parser.LanguageBaseVisitor;
import ru.ifmo.ctddev.khovanskiy.compilers.parser.LanguageParser;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author victor
 */
@Slf4j
public class Parser extends LanguageBaseVisitor<AST> {
    @Override
    public AST.CompilationUnit visitCompilationUnit(LanguageParser.CompilationUnitContext ctx) {
        AST.CompoundStatement compoundStatement = visitCompoundStatement(ctx.compoundStatement());
        return new AST.CompilationUnit(compoundStatement);
    }

    @Override
    public AST.CompoundStatement visitCompoundStatement(LanguageParser.CompoundStatementContext ctx) {
        List<AST.SingleStatement> singleStatementList = ctx.singleStatement().stream()
                .map(this::visitSingleStatement)
                .collect(Collectors.toList());
        return new AST.CompoundStatement(singleStatementList);
    }

    @Override
    public AST.SingleStatement visitSingleStatement(LanguageParser.SingleStatementContext ctx) {
        /*if (ctx.expressionStatement() != null) {
            return visitExpressionStatement(ctx.expressionStatement());
        }*/
        if (ctx.iterationStatement() != null) {
            if (ctx.iterationStatement() instanceof LanguageParser.WhileContext) {
                return visitWhile((LanguageParser.WhileContext) ctx.iterationStatement());
            }
            if (ctx.iterationStatement() instanceof LanguageParser.RepeatContext) {
                return visitRepeat((LanguageParser.RepeatContext) ctx.iterationStatement());
            }
            if (ctx.iterationStatement() instanceof LanguageParser.ForContext) {
                return visitFor((LanguageParser.ForContext) ctx.iterationStatement());
            }
        }
//        if (ctx.selectionStatement() != null) {
//            if (ctx.selectionStatement() instanceof LanguageParser.IfContext) {
//                return visitIf((LanguageParser.IfContext) ctx.selectionStatement());
//            }
//        }
        throw new IllegalArgumentException();
    }

    /*@Override
    public AST.IfStatement visitIf(LanguageParser.IfContext ctx) {
        List<AST.Expression> conditions = ctx.expression().stream()
                .map(this::visitExpression)
                .collect(Collectors.toList());
        List<AST.CompoundStatement> compoundStatements = ctx.compoundStatement().stream()
                .map(this::visitCompoundStatement)
                .collect(Collectors.toList());
        return new AST.IfStatement(conditions, compoundStatements);
    }*/



    @Override
    public AST.WhileStatement visitWhile(LanguageParser.WhileContext ctx) {
        AST.Expression condition = visitExpression(ctx.expression());
        AST.CompoundStatement compoundStatement = visitCompoundStatement(ctx.compoundStatement());
        return new AST.WhileStatement(condition, compoundStatement);
    }

    @Override
    public AST.RepeatStatement visitRepeat(LanguageParser.RepeatContext ctx) {
        AST.CompoundStatement compoundStatement = visitCompoundStatement(ctx.compoundStatement());
        AST.Expression condition = visitExpression(ctx.expression());
        return new AST.RepeatStatement(compoundStatement, condition);
    }

    @Override
    public AST.ForStatement visitFor(LanguageParser.ForContext ctx) {
        AST.AssignmentStatement init = visitAssignmentStatement(ctx.init);
        AST.Expression condition = visitExpression(ctx.condition);
        AST.AssignmentStatement loop = visitAssignmentStatement(ctx.loop);
        AST.CompoundStatement compoundStatement = visitCompoundStatement(ctx.compoundStatement());
        return new AST.ForStatement(init, condition, loop, compoundStatement);
    }

    @Override
    public AST.ExpressionStatement visitExpressionStatement(LanguageParser.ExpressionStatementContext ctx) {
        AST.Expression expression = visitFunctionCall(ctx.functionCall());
        return new AST.ExpressionStatement(expression);
    }

    @Override
    public AST.Expression visitExpression(LanguageParser.ExpressionContext ctx) {
        if (ctx.binaryOperator != null) {
            AST.Expression left = visitExpression(ctx.left);
            AST.Expression right = visitExpression(ctx.right);
            String operator = ctx.binaryOperator.getText();
            return new AST.BinaryExpression(operator, left, right);
        }
        if (ctx.unaryOperator != null) {
            String operator = ctx.unaryOperator.getText();
            AST.Expression expression = visitExpression(ctx.expression(0));
            return new AST.UnaryExpression(operator, expression);
        }
        /*if (ctx.assignment() != null) {
            return visitAssignment(ctx.assignment());
        }*/
        if (ctx.memoryAccess() != null) {
            return visitMemoryAccess(ctx.memoryAccess());
        }
        if (ctx.literal() != null) {
            return visitLiteral(ctx.literal());
        }
        if (ctx.functionCall() != null) {
            return visitFunctionCall(ctx.functionCall());
        }
        return visitExpression(ctx.expression(0));
    }

    @Override
    public AST.FunctionCall visitFunctionCall(LanguageParser.FunctionCallContext ctx) {
        String name = ctx.Identifier().getSymbol().getText().toLowerCase();
        List<AST.Expression> arguments = ctx.argumentList().list;
        return new AST.FunctionCall(name, arguments);
    }

    @Override
    public AST.Literal visitLiteral(LanguageParser.LiteralContext ctx) {
        if (ctx.IntegerLiteral() != null) {
            Integer value = Integer.parseInt(ctx.IntegerLiteral().getSymbol().getText());
            return new AST.IntegerLiteral(value);
        }
//        if (ctx.CharacterLiteral() != null) {
//            Character value = ctx.CharacterLiteral().getSymbol().getText().charAt(1);
//            return new AST.CharacterLiteral(value);
//        }
//        if (ctx.StringLiteral() != null) {
//            String value = ctx.StringLiteral().getSymbol().getText();
//            value = value.substring(1, value.length() - 1);
//            return new AST.StringLiteral(value);
//        }
        throw new IllegalArgumentException();
    }

    @Override
    public AST.MemoryAccessExpression visitMemoryAccess(LanguageParser.MemoryAccessContext ctx) {
        if (ctx.arrayAccess() != null) {
            return visitArrayAccess(ctx.arrayAccess());
        }
        if (ctx.variableAccess() != null) {
            return visitVariableAccess(ctx.variableAccess());
        }
        throw new IllegalArgumentException();
    }

    @Override
    public AST.VariableAccessExpression visitVariableAccess(LanguageParser.VariableAccessContext ctx) {
        String name = ctx.Identifier().getSymbol().getText();
        return new AST.VariableAccessExpression(name);
    }

    @Override
    public AST.ArrayAccessExpression visitArrayAccess(LanguageParser.ArrayAccessContext ctx) {
        //String name = ctx.Identifier().getSymbol().getText();
        //List<AST.Expression> expressions = ctx.expression().stream().map(this::visitExpression).collect(Collectors.toList());
        //if (ctx.variableAccess() )
        //return new AST.ArrayAccessExpression(name, expressions);
        throw new UnsupportedOperationException();
    }

    @Override
    public AST.AssignmentStatement visitAssignmentStatement(LanguageParser.AssignmentStatementContext ctx) {
        AST.MemoryAccessExpression memoryAccess = visitMemoryAccess(ctx.memoryAccess());
        AST.Expression expression = visitExpression(ctx.expression());
        return new AST.AssignmentStatement(memoryAccess, expression);
    }
}
