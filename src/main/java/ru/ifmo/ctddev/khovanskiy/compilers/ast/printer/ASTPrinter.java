package ru.ifmo.ctddev.khovanskiy.compilers.ast.printer;

import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.visitor.AbstractASTVisitor;

import java.io.IOException;
import java.util.List;

public class ASTPrinter extends AbstractASTVisitor<PrinterContext> {
    @Override
    public void visitCompilationUnit(AST.CompilationUnit compilationUnit, PrinterContext writer) throws Exception {
        super.visitCompilationUnit(compilationUnit, writer);
        writer.flush();
    }

    @Override
    public void visitCompoundStatement(AST.CompoundStatement compoundStatement, PrinterContext context) throws Exception {
        for (AST.SingleStatement singleStatement : compoundStatement.getStatements()) {
            visitSingleStatement(singleStatement, context);
            context.append(";\n");
        }
    }

    @Override
    public void visitFunctionDefinition(AST.FunctionDefinition functionDefinition, PrinterContext context) throws Exception {
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
        visitCompoundStatement(functionDefinition.getCompoundStatement(), new PrinterContext(context));
        context.append("end");
    }

    @Override
    public void visitVariableDefinition(AST.VariableDefinition variableDefinition, PrinterContext context) throws IOException {
        context.append(variableDefinition.getName());
    }

    @Override
    public void visitAssignmentStatement(AST.AssignmentStatement assignmentStatement, PrinterContext writer) throws Exception {
        visitMemoryAccess(assignmentStatement.getMemoryAccess(), writer);
        writer.append(" := ");
        visitExpression(assignmentStatement.getExpression(), writer);
    }

    @Override
    public void visitIfStatement(AST.IfStatement ifStatement, PrinterContext writer) throws Exception {
        List<AST.IfCase> cases = ifStatement.getCases();
        for (int i = 0; i < cases.size(); i++) {
            AST.IfCase ifCase = cases.get(i);
            if (i == 0) {
                writer.append("if ");
                visitExpression(ifCase.getCondition(), writer);
                writer.append(" then\n");
                visitCompoundStatement(ifCase.getCompoundStatement(), new PrinterContext(writer));
            } else if (ifCase.getCondition() == null) {
                writer.append("else ");
                visitCompoundStatement(ifCase.getCompoundStatement(), new PrinterContext(writer));
            } else {
                writer.append("elif ");
                visitExpression(ifCase.getCondition(), writer);
                writer.append(" then\n");
                visitCompoundStatement(ifCase.getCompoundStatement(), new PrinterContext(writer));
            }
        }
        writer.append("fi");
    }

    @Override
    public void visitGotoStatement(AST.GotoStatement gotoStatement, PrinterContext context) throws IOException {
        context.append("goto " + gotoStatement.getLabel());
    }

    @Override
    public void visitLabelStatement(AST.LabelStatement labelStatement, PrinterContext context) throws IOException {
        context.append(labelStatement + ":");
    }

    @Override
    public void visitContinueStatement(AST.ContinueStatement continueStatement, PrinterContext context) throws IOException {
        context.append("continue");
    }

    @Override
    public void visitBreakStatement(AST.BreakStatement breakStatement, PrinterContext context) throws IOException {
        context.append("break");
    }

    @Override
    public void visitReturnStatement(AST.ReturnStatement returnStatement, PrinterContext context) throws Exception {
        context.append("return ");
        if (returnStatement.getExpression() != null) {
            visitExpression(returnStatement.getExpression(), context);
        }
    }

    @Override
    public void visitSkipStatement(AST.SkipStatement skipStatement, PrinterContext context) throws IOException {
        context.append("skip");
    }

    @Override
    public void visitWhileStatement(AST.WhileStatement whileStatement, PrinterContext writer) throws Exception {
        writer.append("while ");
        visitExpression(whileStatement.getCondition(), writer);
        writer.append(" do\n");
        visitCompoundStatement(whileStatement.getCompoundStatement(), new PrinterContext(writer));
        writer.append("od");
    }

    @Override
    public void visitRepeatStatement(AST.RepeatStatement repeatStatement, PrinterContext writer) throws Exception {
        writer.append("repeat\n");
        visitCompoundStatement(repeatStatement.getCompoundStatement(), new PrinterContext(writer));
        writer.append("until ");
        visitExpression(repeatStatement.getCondition(), writer);
    }

    @Override
    public void visitForStatement(AST.ForStatement forStatement, PrinterContext writer) throws Exception {
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
        visitCompoundStatement(forStatement.getCompoundStatement(), new PrinterContext(writer));
        writer.append("od");
    }

    @Override
    public void visitFunctionCall(AST.FunctionCall functionCall, PrinterContext writer) throws Exception {
        writer.append(functionCall.getName()).append("(");
        List<AST.Expression> arguments = functionCall.getArguments();
        iterateExpressions(writer, arguments);
        writer.append(")");
    }

    @Override
    public void visitArrayCreation(AST.ArrayCreationExpression arrayCreationExpression, PrinterContext context) throws Exception {
        context.append("[");
        List<AST.Expression> arguments = arrayCreationExpression.getArguments();
        iterateExpressions(context, arguments);
        context.append("]");
    }

    protected void iterateExpressions(PrinterContext context, List<AST.Expression> arguments) throws Exception {
        for (int i = 0; i < arguments.size(); ++i) {
            AST.Expression expression = arguments.get(i);
            visitExpression(expression, context);
            if (i < arguments.size() - 1) {
                context.append(", ");
            }
        }
    }

    @Override
    public void visitVariableAccess(AST.VariableAccessExpression variableAccessExpression, PrinterContext writer) throws Exception {
        writer.append(variableAccessExpression.getName());
    }

    @Override
    public void visitArrayAccess(AST.ArrayAccessExpression arrayAccessExpression, PrinterContext writer) throws Exception {
        visitMemoryAccess(arrayAccessExpression.getPointer(), writer);
        writer.append("[");
        visitExpression(arrayAccessExpression.getExpression(), writer);
        writer.append("]");
    }

    @Override
    public void visitVariableAccessForWrite(AST.VariableAccessExpression variableAccessExpression, PrinterContext context) throws Exception {
        visitVariableAccess(variableAccessExpression, context);
    }

    @Override
    public void visitArrayAccessForWrite(AST.ArrayAccessExpression arrayAccessExpression, PrinterContext context) {
        visitArrayAccessForWrite(arrayAccessExpression, context);
    }

    @Override
    public void visitIntegerLiteral(AST.IntegerLiteral integerLiteral, PrinterContext writer) throws IOException {
        writer.append(String.valueOf(integerLiteral.getValue()));
    }

    @Override
    public void visitCharacterLiteral(AST.CharacterLiteral characterLiteral, PrinterContext writer) throws IOException {
        writer.append("'" + String.valueOf(characterLiteral.getValue()) + "'");
    }

    @Override
    public void visitStringLiteral(AST.StringLiteral stringLiteral, PrinterContext writer) throws IOException {
        writer.append("\"" + stringLiteral.getValue() + "\"");
    }

    @Override
    public void visitNullLiteral(AST.NullLiteral nullLiteral, PrinterContext writer) throws IOException {
        writer.append("null");
    }

    @Override
    public void visitUnaryExpression(AST.UnaryExpression unaryExpression, PrinterContext writer) throws Exception {
        writer.append(unaryExpression.getOperator());
        visitExpression(unaryExpression.getExpression(), writer);
    }

    @Override
    public void visitBinaryExpression(AST.BinaryExpression binaryExpression, PrinterContext writer) throws Exception {
        writer.append("(");
        visitExpression(binaryExpression.getLeft(), writer);
        writer.append(" ").append(binaryExpression.getOperator()).append(" ");
        visitExpression(binaryExpression.getRight(), writer);
        writer.append(")");
    }
}
