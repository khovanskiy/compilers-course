package ru.ifmo.ctddev.khovanskiy.compilers.ast.visitor;

import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;

/**
 * Abstract syntax tree visitor
 *
 * @param <C> the type of context
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public interface ASTVisitor<C> {
    /**
     * Visits the root of program
     *
     * @param compilationUnit the root of program
     * @param context         the context
     * @since 1.0.0
     */
    void visitCompilationUnit(AST.CompilationUnit compilationUnit, C context);

    /**
     * Visits the compound statement
     *
     * @param compoundStatement the compound statement
     * @since 1.0.0
     */
    void visitCompoundStatement(AST.CompoundStatement compoundStatement, C context);

    /**
     * Visits the abstract single statement
     *
     * @param singleStatement the single statement
     * @param context         the context
     * @since 1.0.0
     */
    void visitSingleStatement(AST.SingleStatement singleStatement, C context);

    /**
     * Visits the abstract declaration
     *
     * @param declaration the declaration
     * @param context     the context
     * @since 1.0.0
     */
    void visitDeclaration(AST.Declaration declaration, C context);

    /**
     * Visits the function definition
     *
     * @param functionDefinition the function definition
     * @param context            the context
     * @since 1.0.0
     */
    void visitFunctionDefinition(AST.FunctionDefinition functionDefinition, C context);

    /**
     * Visits the variable definition
     *
     * @param variableDefinition the variable definition
     * @param context            the context
     * @since 1.0.0
     */
    void visitVariableDefinition(AST.VariableDefinition variableDefinition, C context);

    /**
     * Visits the assignment statement
     *
     * @param assignmentStatement the assignment statement
     * @param context             the context
     * @since 1.0.0
     */
    void visitAssignmentStatement(AST.AssignmentStatement assignmentStatement, C context);

    /**
     * Visits the expression statement
     *
     * @param expressionStatement the expression statement
     * @param context             the context
     * @since 1.0.0
     */
    void visitExpressionStatement(AST.ExpressionStatement expressionStatement, C context);

    /**
     * Visits the abstract selection statement
     *
     * @param selectionStatement the selection statement
     * @param context            the context
     * @since 1.0.0
     */
    void visitSelectionStatement(AST.SelectionStatement selectionStatement, C context);

    /**
     * Visits the "if" statement
     *
     * @param ifStatement the "if" statement
     * @param context     the context
     * @since 1.0.0
     */
    void visitIfStatement(AST.IfStatement ifStatement, C context);

    /**
     * Visits the abstract jump statement
     *
     * @param jumpStatement the jump statement
     * @param context       the context
     * @since 1.0.0
     */
    void visitJumpStatement(AST.JumpStatement jumpStatement, C context);

    /**
     * Visits the unconditional "goto" jump
     *
     * @param gotoStatement the "goto" statement
     * @param context       the context
     * @since 1.0.0
     */
    void visitGotoStatement(AST.GotoStatement gotoStatement, C context);

    /**
     * Visits the "label" statement
     *
     * @param labelStatement the "label" statement
     * @since 1.0.0
     */
    void visitLabelStatement(AST.LabelStatement labelStatement, C context);

    /**
     * Visits the "continue" statement
     *
     * @param continueStatement the "continue" statement
     * @since 1.0.0
     */
    void visitContinueStatement(AST.ContinueStatement continueStatement, C context);

    /**
     * Visits the "break" statement
     *
     * @param breakStatement the "break" statement
     * @since 1.0.0
     */
    void visitBreakStatement(AST.BreakStatement breakStatement, C context);

    /**
     * Visits the "return" statement
     *
     * @param returnStatement the "return" statement
     * @param context         the context
     * @since 1.0.0
     */
    void visitReturnStatement(AST.ReturnStatement returnStatement, C context);

    /**
     * Visits the "skip" statement
     *
     * @param skipStatement the "skip" statement
     * @param context       the context
     * @since 1.0.0
     */
    void visitSkipStatement(AST.SkipStatement skipStatement, C context);

    /**
     * Visits the abstract iteration statement
     *
     * @param iterationStatement the iteration statement
     * @param context            the context
     * @since 1.0.0
     */
    void visitIterationStatement(AST.IterationStatement iterationStatement, C context);

    /**
     * Visits the "while" statement
     *
     * @param whileStatement the "while" statement
     * @param context        the context
     * @since 1.0.0
     */
    void visitWhileStatement(AST.WhileStatement whileStatement, C context);

    /**
     * Visits the "repeat" statement
     *
     * @param repeatStatement the "repeat" statement
     * @param context         the context
     * @since 1.0.0
     */
    void visitRepeatStatement(AST.RepeatStatement repeatStatement, C context);

    /**
     * Visits the "for" statement
     *
     * @param forStatement the "for" statement
     * @param context      the context
     * @since 1.0.0
     */
    void visitForStatement(AST.ForStatement forStatement, C context);

    /**
     * Visits the abstract expression
     *
     * @param expression the expression
     * @param context    the context
     * @since 1.0.0
     */
    void visitExpression(AST.Expression expression, C context);

    /**
     * Visits the function call expression
     *
     * @param functionCall the function call expression
     * @param context      the context
     * @since 1.0.0
     */
    void visitFunctionCall(AST.FunctionCall functionCall, C context);

    /**
     * @param arrayCreationExpression the array creation expression
     * @param context                 the context
     * @since 1.0.0
     */
    void visitArrayCreation(AST.ArrayCreationExpression arrayCreationExpression, C context);

    /**
     * Visits the abstract memory access expression for read
     *
     * @param memoryAccessExpression the memory access expression
     * @param context                the context
     * @since 1.0.0
     */
    void visitMemoryAccessForRead(AST.MemoryAccessExpression memoryAccessExpression, C context);

    /**
     * Visits the variable access expression for read
     *
     * @param variableAccessExpression the variable access expression
     * @param context                  the context
     * @since 1.0.0
     */
    void visitVariableAccessForRead(AST.VariableAccessExpression variableAccessExpression, C context);

    /**
     * Visits the array access expression for read
     *
     * @param arrayAccessExpression the array access expression
     * @param context               the context
     * @since 1.0.0
     */
    void visitArrayAccessForRead(AST.ArrayAccessExpression arrayAccessExpression, C context);

    /**
     * Visits the abstract memory access expression for write
     *
     * @param memoryAccessExpression the memory access expression
     * @param context                the context
     * @since 1.0.0
     */
    void visitMemoryAccessForWrite(AST.MemoryAccessExpression memoryAccessExpression, C context);

    /**
     * Visits the variable access expression for write
     *
     * @param variableAccessExpression the variable access expression
     * @param context                  the context
     * @since 1.0.0
     */
    void visitVariableAccessForWrite(AST.VariableAccessExpression variableAccessExpression, C context);

    /**
     * Visits the array access expression for write
     *
     * @param arrayAccessExpression the array access expression
     * @param context               the context
     * @since 1.0.0
     */
    void visitArrayAccessForWrite(AST.ArrayAccessExpression arrayAccessExpression, C context);

    /**
     * Visits the abstract literal
     *
     * @param literal the literal
     * @param context the context
     * @since 1.0.0
     */
    void visitLiteral(AST.Literal literal, C context);

    /**
     * Visits the integer literal
     *
     * @param integerLiteral the integer literal
     * @param context        the context
     * @since 1.0.0
     */
    void visitIntegerLiteral(AST.IntegerLiteral integerLiteral, C context);

    /**
     * Visits the character literal
     *
     * @param characterLiteral the character literal
     * @param context          the context
     * @since 1.0.0
     */
    void visitCharacterLiteral(AST.CharacterLiteral characterLiteral, C context);

    /**
     * Visits the string literal
     *
     * @param stringLiteral the string literal
     * @param context       the context
     * @since 1.0.0
     */
    void visitStringLiteral(AST.StringLiteral stringLiteral, C context);

    /**
     * Visits the null reference literal
     *
     * @param nullLiteral the null reference literal
     * @param context     the context
     * @since 1.0.0
     */
    void visitNullLiteral(AST.NullLiteral nullLiteral, C context);

    /**
     * Visits the unary expression
     *
     * @param unaryExpression the unary expression
     * @param context         the context
     * @since 1.0.0
     */
    void visitUnaryExpression(AST.UnaryExpression unaryExpression, C context);

    /**
     * Visits the binary expression
     *
     * @param binaryExpression the binary expression
     * @param context          the context
     * @since 1.0.0
     */
    void visitBinaryExpression(AST.BinaryExpression binaryExpression, C context);
}
