package ru.ifmo.ctddev.khovanskiy.compilers.ast.visitor;

import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;

public abstract class AbstractASTVisitor<C> implements ASTVisitor<C> {
    @Override
    public void visitCompilationUnit(AST.CompilationUnit compilationUnit, C c) throws Exception {
        visitCompoundStatement(compilationUnit.getCompoundStatement(), c);
    }

    @Override
    public void visitCompoundStatement(AST.CompoundStatement compoundStatement, C c) throws Exception {
        for (AST.SingleStatement singleStatement : compoundStatement.getStatements()) {
            visitSingleStatement(singleStatement, c);
        }
    }

    @Override
    public void visitSingleStatement(AST.SingleStatement singleStatement, C c) throws Exception {
        if (singleStatement instanceof AST.Declaration) {
            visitDeclaration((AST.Declaration) singleStatement, c);
            return;
        }
        if (singleStatement instanceof AST.AssignmentStatement) {
            visitAssignmentStatement((AST.AssignmentStatement) singleStatement, c);
            return;
        }
        if (singleStatement instanceof AST.ExpressionStatement) {
            visitExpressionStatement((AST.ExpressionStatement) singleStatement, c);
            return;
        }
        if (singleStatement instanceof AST.SelectionStatement) {
            visitSelectionStatement((AST.SelectionStatement) singleStatement, c);
            return;
        }
        if (singleStatement instanceof AST.JumpStatement) {
            visitJumpStatement((AST.JumpStatement) singleStatement, c);
            return;
        }
        if (singleStatement instanceof AST.IterationStatement) {
            visitIterationStatement((AST.IterationStatement) singleStatement, c);
            return;
        }
        throw new IllegalStateException("Unknown statement type: " + singleStatement.getClass());
    }

    @Override
    public void visitDeclaration(AST.Declaration declaration, C c) throws Exception {
        if (declaration instanceof AST.FunctionDefinition) {
            visitFunctionDefinition((AST.FunctionDefinition) declaration, c);
            return;
        }
        if (declaration instanceof AST.VariableDefinition) {
            visitVariableDefinition((AST.VariableDefinition) declaration, c);
            return;
        }
        throw new IllegalStateException("Unknown declaration type: " + declaration.getClass());
    }

    @Override
    public void visitExpressionStatement(AST.ExpressionStatement expressionStatement, C c) throws Exception {
        visitExpression(expressionStatement.getExpression(), c);
    }

    @Override
    public void visitSelectionStatement(AST.SelectionStatement selectionStatement, C c) throws Exception {
        if (selectionStatement instanceof AST.IfStatement) {
            visitIfStatement((AST.IfStatement) selectionStatement, c);
            return;
        }
        throw new IllegalStateException("Unknown selection statement type: " + selectionStatement.getClass());
    }

    @Override
    public void visitJumpStatement(AST.JumpStatement jumpStatement, C c) throws Exception {
        if (jumpStatement instanceof AST.GotoStatement) {
            visitGotoStatement((AST.GotoStatement) jumpStatement, c);
            return;
        }
        if (jumpStatement instanceof AST.LabelStatement) {
            visitLabelStatement((AST.LabelStatement) jumpStatement, c);
            return;
        }
        if (jumpStatement instanceof AST.ContinueStatement) {
            visitContinueStatement((AST.ContinueStatement) jumpStatement, c);
            return;
        }
        if (jumpStatement instanceof AST.BreakStatement) {
            visitBreakStatement((AST.BreakStatement) jumpStatement, c);
            return;
        }
        if (jumpStatement instanceof AST.ReturnStatement) {
            visitReturnStatement((AST.ReturnStatement) jumpStatement, c);
            return;
        }
        if (jumpStatement instanceof AST.SkipStatement) {
            visitSkipStatement((AST.SkipStatement) jumpStatement, c);
            return;
        }
        throw new IllegalStateException("Unknown jump statement type: " + jumpStatement.getClass());
    }

    @Override
    public void visitIterationStatement(AST.IterationStatement iterationStatement, C c) throws Exception {
        if (iterationStatement instanceof AST.WhileStatement) {
            visitWhileStatement((AST.WhileStatement) iterationStatement, c);
            return;
        }
        if (iterationStatement instanceof AST.RepeatStatement) {
            visitRepeatStatement((AST.RepeatStatement) iterationStatement, c);
            return;
        }
        if (iterationStatement instanceof AST.ForStatement) {
            visitForStatement((AST.ForStatement) iterationStatement, c);
            return;
        }
        throw new IllegalStateException("Unknown iteration statement type: " + iterationStatement.getClass());
    }

    @Override
    public void visitExpression(AST.Expression expression, C c) throws Exception {
        if (expression instanceof AST.FunctionCall) {
            visitFunctionCall((AST.FunctionCall) expression, c);
            return;
        }
        if (expression instanceof AST.ArrayCreationExpression) {
            visitArrayCreation((AST.ArrayCreationExpression) expression, c);
            return;
        }
        if (expression instanceof AST.MemoryAccessExpression) {
            visitMemoryAccess((AST.MemoryAccessExpression) expression, c);
            return;
        }
        if (expression instanceof AST.Literal) {
            visitLiteral((AST.Literal) expression, c);
            return;
        }
        if (expression instanceof AST.UnaryExpression) {
            visitUnaryExpression((AST.UnaryExpression) expression, c);
            return;
        }
        if (expression instanceof AST.BinaryExpression) {
            visitBinaryExpression((AST.BinaryExpression) expression, c);
            return;
        }
        throw new IllegalStateException("Unknown expression type: " + expression.getClass());
    }

    @Override
    public void visitMemoryAccess(AST.MemoryAccessExpression memoryAccessExpression, C c) throws Exception {
        if (memoryAccessExpression instanceof AST.VariableAccessExpression) {
            visitVariableAccess((AST.VariableAccessExpression) memoryAccessExpression, c);
            return;
        }
        if (memoryAccessExpression instanceof AST.ArrayAccessExpression) {
            visitArrayAccess((AST.ArrayAccessExpression) memoryAccessExpression, c);
            return;
        }
        throw new IllegalStateException("Unknown memory access type: " + memoryAccessExpression.getClass());
    }

    @Override
    public void visitMemoryAccessForWrite(AST.MemoryAccessExpression memoryAccessExpression, C c) throws Exception {
        if (memoryAccessExpression instanceof AST.VariableAccessExpression) {
            visitVariableAccessForWrite((AST.VariableAccessExpression) memoryAccessExpression, c);
            return;
        }
        if (memoryAccessExpression instanceof AST.ArrayAccessExpression) {
            visitArrayAccessForWrite((AST.ArrayAccessExpression) memoryAccessExpression, c);
            return;
        }
        throw new IllegalStateException("Unknown memory access type: " + memoryAccessExpression.getClass());
    }

    @Override
    public void visitLiteral(AST.Literal literal, C c) throws Exception {
        if (literal instanceof AST.IntegerLiteral) {
            visitIntegerLiteral((AST.IntegerLiteral) literal, c);
            return;
        }
        if (literal instanceof AST.CharacterLiteral) {
            visitCharacterLiteral((AST.CharacterLiteral) literal, c);
            return;
        }
        if (literal instanceof AST.StringLiteral) {
            visitStringLiteral((AST.StringLiteral) literal, c);
            return;
        }
        if (literal instanceof AST.NullLiteral) {
            visitNullLiteral((AST.NullLiteral) literal, c);
            return;
        }
        throw new IllegalStateException("Unknown literal type: " + literal.getClass());
    }
}
