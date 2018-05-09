package ru.ifmo.ctddev.khovanskiy.compilers.ast.printer;

import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.visitor.AbstractASTVisitor;

import java.io.Writer;
import java.util.List;

/**
 * Abstract syntax tree printer
 *
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class ASTPrinter extends AbstractASTVisitor<ASTPrinterContext> {
    /**
     * Prints the program
     *
     * @param ast    the ast of program
     * @param writer the IO writer
     * @since 1.0.0
     */
    public void print(AST.CompilationUnit ast, Writer writer) {
        visitCompilationUnit(ast, new ASTPrinterContext(writer));
    }

    @Override
    public void visitCompilationUnit(AST.CompilationUnit compilationUnit, ASTPrinterContext context) {
        super.visitCompilationUnit(compilationUnit, context);
        context.flush();
    }

    @Override
    public void visitCompoundStatement(AST.CompoundStatement compoundStatement, ASTPrinterContext context) {
        for (AST.SingleStatement singleStatement : compoundStatement.getStatements()) {
            visitSingleStatement(singleStatement, context);
            context.append(";\n");
        }
    }

    @Override
    public void visitFunctionDefinition(AST.FunctionDefinition functionDefinition, ASTPrinterContext context) {
        context.append("fun " + functionDefinition.getName() + " (");
        final List<AST.VariableDefinition> arguments = functionDefinition.getVariables();
        for (int i = 0; i < arguments.size(); ++i) {
            AST.VariableDefinition expression = arguments.get(i);
            visitVariableDefinition(expression, context);
            if (i < arguments.size() - 1) {
                context.append(", ");
            }
        }
        context.append(")\n");
        context.append("begin\n");
        visitCompoundStatement(functionDefinition.getCompoundStatement(), new ASTPrinterContext(context));
        context.append("end");
    }

    @Override
    public void visitVariableDefinition(AST.VariableDefinition variableDefinition, ASTPrinterContext context) {
        context.append(variableDefinition.getName());
    }

    @Override
    public void visitAssignmentStatement(AST.AssignmentStatement assignmentStatement, ASTPrinterContext writer) {
        visitMemoryAccessForRead(assignmentStatement.getMemoryAccess(), writer);
        writer.append(" := ");
        visitExpression(assignmentStatement.getExpression(), writer);
    }

    @Override
    public void visitIfStatement(AST.IfStatement ifStatement, ASTPrinterContext writer) {
        List<AST.IfCase> cases = ifStatement.getCases();
        for (int i = 0; i < cases.size(); i++) {
            AST.IfCase ifCase = cases.get(i);
            if (i == 0) {
                writer.append("if ");
                visitExpression(ifCase.getCondition(), writer);
                writer.append(" then\n");
                visitCompoundStatement(ifCase.getCompoundStatement(), new ASTPrinterContext(writer));
            } else if (ifCase.getCondition() == null) {
                writer.append("else ");
                visitCompoundStatement(ifCase.getCompoundStatement(), new ASTPrinterContext(writer));
            } else {
                writer.append("elif ");
                visitExpression(ifCase.getCondition(), writer);
                writer.append(" then\n");
                visitCompoundStatement(ifCase.getCompoundStatement(), new ASTPrinterContext(writer));
            }
        }
        writer.append("fi");
    }

    @Override
    public void visitGotoStatement(AST.GotoStatement gotoStatement, ASTPrinterContext context) {
        context.append("goto " + gotoStatement.getLabel());
    }

    @Override
    public void visitLabelStatement(AST.LabelStatement labelStatement, ASTPrinterContext context) {
        context.append(labelStatement + ":");
    }

    @Override
    public void visitContinueStatement(AST.ContinueStatement continueStatement, ASTPrinterContext context) {
        context.append("continue");
    }

    @Override
    public void visitBreakStatement(AST.BreakStatement breakStatement, ASTPrinterContext context) {
        context.append("break");
    }

    @Override
    public void visitReturnStatement(AST.ReturnStatement returnStatement, ASTPrinterContext context) {
        context.append("return ");
        if (returnStatement.getExpression() != null) {
            visitExpression(returnStatement.getExpression(), context);
        }
    }

    @Override
    public void visitSkipStatement(AST.SkipStatement skipStatement, ASTPrinterContext context) {
        context.append("skip");
    }

    @Override
    public void visitWhileStatement(AST.WhileStatement whileStatement, ASTPrinterContext writer) {
        writer.append("while ");
        visitExpression(whileStatement.getCondition(), writer);
        writer.append(" do\n");
        visitCompoundStatement(whileStatement.getCompoundStatement(), new ASTPrinterContext(writer));
        writer.append("od");
    }

    @Override
    public void visitRepeatStatement(AST.RepeatStatement repeatStatement, ASTPrinterContext writer) {
        writer.append("repeat\n");
        visitCompoundStatement(repeatStatement.getCompoundStatement(), new ASTPrinterContext(writer));
        writer.append("until ");
        visitExpression(repeatStatement.getCondition(), writer);
    }

    @Override
    public void visitForStatement(AST.ForStatement forStatement, ASTPrinterContext writer) {
        writer.append("for ");
        if (forStatement.getInit() != null) {
            visitSingleStatement(forStatement.getInit(), writer);
        }
        writer.append(", ");
        visitExpression(forStatement.getCondition(), writer);
        writer.append(", ");
        if (forStatement.getLoop() != null) {
            visitSingleStatement(forStatement.getLoop(), writer);
        }
        writer.append(" do\n");
        visitCompoundStatement(forStatement.getCompoundStatement(), new ASTPrinterContext(writer));
        writer.append("od");
    }

    @Override
    public void visitFunctionCall(AST.FunctionCall functionCall, ASTPrinterContext writer) {
        writer.append(functionCall.getName()).append("(");
        List<AST.Expression> arguments = functionCall.getArguments();
        iterateExpressions(writer, arguments);
        writer.append(")");
    }

    @Override
    public void visitArrayCreation(AST.ArrayCreationExpression arrayCreationExpression, ASTPrinterContext context) {
        context.append("[");
        List<AST.Expression> arguments = arrayCreationExpression.getArguments();
        iterateExpressions(context, arguments);
        context.append("]");
    }

    @Override
    public void visitVariableAccessForRead(AST.VariableAccessExpression variableAccessExpression, ASTPrinterContext writer) {
        writer.append(variableAccessExpression.getName());
    }

    @Override
    public void visitArrayAccessForRead(AST.ArrayAccessExpression arrayAccessExpression, ASTPrinterContext writer) {
        visitMemoryAccessForRead(arrayAccessExpression.getPointer(), writer);
        writer.append("[");
        visitExpression(arrayAccessExpression.getExpression(), writer);
        writer.append("]");
    }

    @Override
    public void visitVariableAccessForWrite(AST.VariableAccessExpression variableAccessExpression, ASTPrinterContext context) {
        visitVariableAccessForRead(variableAccessExpression, context);
    }

    @Override
    public void visitArrayAccessForWrite(AST.ArrayAccessExpression arrayAccessExpression, ASTPrinterContext context) {
        visitArrayAccessForRead(arrayAccessExpression, context);
    }

    @Override
    public void visitIntegerLiteral(AST.IntegerLiteral integerLiteral, ASTPrinterContext writer) {
        writer.append(String.valueOf(integerLiteral.getValue()));
    }

    @Override
    public void visitCharacterLiteral(AST.CharacterLiteral characterLiteral, ASTPrinterContext writer) {
        writer.append("'" + String.valueOf(characterLiteral.getValue()) + "'");
    }

    @Override
    public void visitStringLiteral(AST.StringLiteral stringLiteral, ASTPrinterContext writer) {
        writer.append("\"" + stringLiteral.getValue() + "\"");
    }

    @Override
    public void visitNullLiteral(AST.NullLiteral nullLiteral, ASTPrinterContext writer) {
        writer.append("null");
    }

    @Override
    public void visitUnaryExpression(AST.UnaryExpression unaryExpression, ASTPrinterContext writer) {
        writer.append(unaryExpression.getOperator());
        visitExpression(unaryExpression.getExpression(), writer);
    }

    @Override
    public void visitBinaryExpression(AST.BinaryExpression binaryExpression, ASTPrinterContext writer) {
        writer.append("(");
        visitExpression(binaryExpression.getLeft(), writer);
        writer.append(" ").append(binaryExpression.getOperator()).append(" ");
        visitExpression(binaryExpression.getRight(), writer);
        writer.append(")");
    }

    /**
     * Prints the list of expressions
     *
     * @param context   the printer context
     * @param arguments the list of expressions
     * @since 1.0.0
     */
    private void iterateExpressions(ASTPrinterContext context, List<AST.Expression> arguments) {
        for (int i = 0; i < arguments.size(); ++i) {
            AST.Expression expression = arguments.get(i);
            visitExpression(expression, context);
            if (i < arguments.size() - 1) {
                context.append(", ");
            }
        }
    }
}
