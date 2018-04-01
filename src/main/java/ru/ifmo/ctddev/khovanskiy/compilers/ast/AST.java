package ru.ifmo.ctddev.khovanskiy.compilers.ast;

import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@ToString
public abstract class AST {

    @Getter
    @ToString
    public static class CompilationUnit extends AST {
        private final CompoundStatement compoundStatement;

        public CompilationUnit(CompoundStatement compoundStatement) {
            this.compoundStatement = compoundStatement;
        }
    }

    @Getter
    @ToString
    public static class CompoundStatement extends AST {
        private final List<SingleStatement> statements;

        public CompoundStatement(List<SingleStatement> statements) {
            this.statements = statements == null ? Collections.emptyList() : statements;
        }
    }

    @Getter
    @ToString
    public static class SingleStatement extends AST {
    }

    @Getter
    @ToString
    public static abstract class Declaration extends SingleStatement {

    }

    @Getter
    @ToString
    public static class FunctionDefinition extends Declaration {
        private final String name;
        private final List<VariableDefinition> variables;
        private final CompoundStatement compoundStatement;

        public FunctionDefinition(final String name,
                                  final List<VariableDefinition> variables,
                                  final CompoundStatement compoundStatement) {
            this.name = name;
            this.variables = variables == null ? Collections.emptyList() : variables;
            this.compoundStatement = compoundStatement;
        }
    }

    @Getter
    @ToString
    public static class VariableDefinition extends Declaration {
        private final String name;

        public VariableDefinition(String name) {
            this.name = name;
        }
    }

    public static abstract class Expression extends AST {
    }

    @Getter
    public static class AssignmentStatement extends SingleStatement {
        private final MemoryAccessExpression memoryAccess;
        private final Expression expression;

        public AssignmentStatement(MemoryAccessExpression memoryAccess, Expression expression) {
            this.memoryAccess = memoryAccess;
            this.expression = expression;
        }

        @Override
        public String toString() {
            return memoryAccess + " := " + expression;
        }
    }

    public static abstract class IterationStatement extends SingleStatement {

    }

    public static abstract class SelectionStatement extends SingleStatement {

    }

    public static abstract class JumpStatement extends SingleStatement {

    }

    @Getter
    @ToString
    public static class GotoStatement extends JumpStatement {
        private final String label;

        public GotoStatement(String label) {
            this.label = label;
        }
    }

    @Getter
    @ToString
    public static class ContinueStatement extends JumpStatement {

    }

    @Getter
    @ToString
    public static class BreakStatement extends JumpStatement {

    }

    @Getter
    @ToString
    public static class ReturnStatement extends JumpStatement {
        private final AST.Expression expression;

        public ReturnStatement(Expression expression) {
            this.expression = expression;
        }
    }

    @Getter
    @ToString
    public static class SkipStatement extends JumpStatement {

    }

    @Getter
    @ToString
    public static class IfStatement extends SelectionStatement {
        /*private final List<AST.Expression> conditions;
        private final List<AST.CompoundStatement> compoundStatements;

        public IfStatement(List<Expression> conditions, List<CompoundStatement> compoundStatements) {
            this.conditions = conditions;
            this.compoundStatements = compoundStatements;
        }*/
        private final List<AST.IfCase> cases;

        public IfStatement(final List<IfCase> cases) {
            this.cases = cases == null ? Collections.emptyList() : cases;
        }
    }

    @Getter
    @ToString
    public static class IfCase {
        private final AST.Expression condition;
        private final AST.CompoundStatement compoundStatement;

        public IfCase(Expression condition, CompoundStatement compoundStatement) {
            this.condition = condition;
            this.compoundStatement = compoundStatement;
        }
    }

    @Getter
    @ToString
    public static class WhileStatement extends IterationStatement {
        private final Expression condition;
        private final CompoundStatement compoundStatement;

        public WhileStatement(Expression condition, CompoundStatement compoundStatement) {
            this.condition = condition;
            this.compoundStatement = compoundStatement;
        }
    }

    @Getter
    @ToString
    public static class RepeatStatement extends IterationStatement {
        private final CompoundStatement compoundStatement;
        private final Expression condition;

        public RepeatStatement(CompoundStatement compoundStatement, Expression condition) {
            this.compoundStatement = compoundStatement;
            this.condition = condition;
        }
    }

    @Getter
    @ToString
    public static class ForStatement extends IterationStatement {
        private final AssignmentStatement init;
        private final Expression condition;
        private final AssignmentStatement loop;
        private final CompoundStatement compoundStatement;

        public ForStatement(AssignmentStatement init, Expression condition, AssignmentStatement loop, CompoundStatement compoundStatement) {
            this.init = init;
            this.condition = condition;
            this.loop = loop;
            this.compoundStatement = compoundStatement;
        }
    }

    @Getter
    @ToString
    public static class ExpressionStatement extends SingleStatement {
        private final AST.Expression expression;

        public ExpressionStatement(Expression expression) {
            this.expression = expression;
        }
    }

    @Getter
    public static class BinaryExpression extends Expression {
        private final String operator;
        private final AST.Expression left;
        private final AST.Expression right;

        public BinaryExpression(String operator, Expression left, Expression right) {
            this.operator = operator;
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return "(" + left + " " + operator + " " + right + ")";
        }
    }

    @Getter
    @ToString
    public static class UnaryExpression extends Expression {
        private final String operator;
        private final AST.Expression expression;

        public UnaryExpression(String operator, Expression expression) {
            this.operator = operator;
            this.expression = expression;
        }
    }

    @Getter
    @ToString
    public static class FunctionCall extends Expression {
        private final String name;
        private final List<Expression> arguments;

        public FunctionCall(String name, List<Expression> arguments) {
            this.name = name;
            this.arguments = arguments == null ? Collections.emptyList() : arguments;
        }
    }

    @Getter
    @ToString
    public static class ArrayCreationExpression extends Expression {
        private final List<Expression> arguments;

        public ArrayCreationExpression(List<Expression> arguments) {
            this.arguments = arguments == null ? Collections.emptyList() : arguments;
        }
    }

    @Getter
    @ToString
    public static abstract class Literal<T> extends Expression {
        private final T value;

        protected Literal(T value) {
            this.value = value;
        }
    }

    @Getter
    public static class IntegerLiteral extends Literal<Integer> {
        public IntegerLiteral(Integer value) {
            super(value);
        }

        @Override
        public String toString() {
            return getValue() + "";
        }
    }

    @Getter
    public static class CharacterLiteral extends Literal<Character> {
        public CharacterLiteral(String value) {
            super(value.charAt(1));
        }

        @Override
        public String toString() {
            return "'" + getValue() + "'";
        }
    }

    @Getter
    public static class StringLiteral extends Literal<String> {
        public StringLiteral(String value) {
            super(value == null || value.isEmpty() ? "" : value.substring(1, value.length() - 1));
        }

        @Override
        public String toString() {
            return "\"" + getValue() + "\"";
        }
    }

    @Getter
    @ToString(callSuper = true)
    public static class NullLiteral extends Literal<Void> {
        public NullLiteral() {
            super(null);
        }
    }

    public static abstract class MemoryAccessExpression extends Expression {

    }

    @Getter
    public static class VariableAccessExpression extends MemoryAccessExpression {
        private final String name;

        public VariableAccessExpression(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Getter
    public static class ArrayAccessExpression extends MemoryAccessExpression {
        private final MemoryAccessExpression pointer;
        private final Expression expression;

        public ArrayAccessExpression(MemoryAccessExpression pointer, Expression index) {
            this.pointer = pointer;
            this.expression = index;
        }

        @Override
        public String toString() {
            return pointer + "[" + expression + "]";
        }
    }
}
