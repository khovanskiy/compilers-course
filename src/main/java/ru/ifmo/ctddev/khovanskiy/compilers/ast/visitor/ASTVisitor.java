package ru.ifmo.ctddev.khovanskiy.compilers.ast.visitor;

import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;

public interface ASTVisitor<C> {
    void visitCompilationUnit(AST.CompilationUnit compilationUnit, C c) throws Exception;

    void visitCompoundStatement(AST.CompoundStatement compoundStatement, C c) throws Exception;

    void visitSingleStatement(AST.SingleStatement singleStatement, C c) throws Exception;

    void visitDeclaration(AST.Declaration declaration, C c) throws Exception;

    void visitFunctionDefinition(AST.FunctionDefinition functionDefinition, C c) throws Exception;

    void visitVariableDefinition(AST.VariableDefinition variableDefinition, C c) throws Exception;

    void visitAssignmentStatement(AST.AssignmentStatement assignmentStatement, C c) throws Exception;

    void visitExpressionStatement(AST.ExpressionStatement expressionStatement, C c) throws Exception;

    void visitSelectionStatement(AST.SelectionStatement selectionStatement, C c) throws Exception;

    void visitIfStatement(AST.IfStatement ifStatement, C c) throws Exception;

    void visitJumpStatement(AST.JumpStatement jumpStatement, C c) throws Exception;

    void visitGotoStatement(AST.GotoStatement gotoStatement, C c) throws Exception;

    void visitContinueStatement(AST.ContinueStatement continueStatement, C c) throws Exception;

    void visitBreakStatement(AST.BreakStatement breakStatement, C c) throws Exception;

    void visitReturnStatement(AST.ReturnStatement returnStatement, C c) throws Exception;

    void visitSkipStatement(AST.SkipStatement skipStatement, C c) throws Exception;

    void visitIterationStatement(AST.IterationStatement iterationStatement, C c) throws Exception;

    void visitWhileStatement(AST.WhileStatement whileStatement, C c) throws Exception;

    void visitRepeatStatement(AST.RepeatStatement repeatStatement, C c) throws Exception;

    void visitForStatement(AST.ForStatement forStatement, C c) throws Exception;

    void visitExpression(AST.Expression expression, C c) throws Exception;

    void visitFunctionCall(AST.FunctionCall functionCall, C c) throws Exception;

    void visitArrayCreation(AST.ArrayCreationExpression arrayCreationExpression, C c) throws Exception;

    void visitMemoryAccess(AST.MemoryAccessExpression memoryAccessExpression, C c) throws Exception;

    void visitVariableAccess(AST.VariableAccessExpression variableAccessExpression, C c) throws Exception;

    void visitArrayAccess(AST.ArrayAccessExpression arrayAccessExpression, C c) throws Exception;

    void visitLiteral(AST.Literal literal, C c) throws Exception;

    void visitIntegerLiteral(AST.IntegerLiteral integerLiteral, C c) throws Exception;

    void visitCharacterLiteral(AST.CharacterLiteral characterLiteral, C c) throws Exception;

    void visitStringLiteral(AST.StringLiteral stringLiteral, C c) throws Exception;

    void visitNullLiteral(AST.NullLiteral nullLiteral, C c) throws Exception;

    void visitUnaryExpression(AST.UnaryExpression unaryExpression, C c) throws Exception;

    void visitBinaryExpression(AST.BinaryExpression binaryExpression, C c) throws Exception;
}
