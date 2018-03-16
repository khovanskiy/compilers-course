package ru.ifmo.ctddev.khovanskiy.compilers;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * @author victor
 */
@ToString
public class AST {

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
            this.statements = statements;
        }
    }

    @Getter
    @ToString
    public static class SingleStatement extends AST {
    }

    public static abstract class Expression extends AST {

    }

    @Getter
    @ToString
    public static class Skip extends Expression {

    }

    public static abstract class Statement extends AST {
    }

    public static abstract class IterationStatement extends SingleStatement {

    }

    public static abstract class SelectionStatement extends SingleStatement {

    }

    @Getter
    @ToString
    public static class IfStatement extends SelectionStatement {
        private final List<AST.Expression> conditions;
        private final List<AST.CompoundStatement> compoundStatements;

        public IfStatement(List<Expression> conditions, List<CompoundStatement> compoundStatements) {
            this.conditions = conditions;
            this.compoundStatements = compoundStatements;
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
        private final Expression init;
        private final Expression condition;
        private final Expression loop;
        private final CompoundStatement compoundStatement;

        public ForStatement(Expression init, Expression condition, Expression loop, CompoundStatement compoundStatement) {
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
    @ToString
    public static class BinaryExpression extends Expression {
        private final String operator;
        private final AST.Expression left;
        private final AST.Expression right;

        public BinaryExpression(String operator, Expression left, Expression right) {
            this.operator = operator;
            this.left = left;
            this.right = right;
        }
    }

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
            this.arguments = arguments;
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
    @ToString(callSuper = true)
    public static class IntegerLiteral extends Literal<Integer> {
        public IntegerLiteral(Integer value) {
            super(value);
        }
    }

    @Getter
    @ToString(callSuper = true)
    public static class CharacterLiteral extends Literal<Character> {
        public CharacterLiteral(Character value) {
            super(value);
        }
    }

    @Getter
    @ToString(callSuper = true)
    public static class StringLiteral extends Literal<String> {
        public StringLiteral(String value) {
            super(value);
        }
    }

    public static abstract class MemoryAccessExpression extends Expression {

    }

    @Getter
    @ToString
    public static class VariableAccessExpression extends MemoryAccessExpression {
        private final String name;

        public VariableAccessExpression(String name) {
            this.name = name;
        }
    }

    @Getter
    @ToString
    public static class ArrayAccessExpression extends MemoryAccessExpression {
        private final String name;
        private final List<Expression> expressions;

        public ArrayAccessExpression(String name, List<Expression> expressions) {
            this.name = name;
            this.expressions = expressions;
        }
    }

    @Getter
    @ToString
    public static class AssignmentExpression extends Expression {
        private final MemoryAccessExpression memoryAccess;
        private final Expression expression;

        public AssignmentExpression(MemoryAccessExpression memoryAccess, Expression expression) {
            this.memoryAccess = memoryAccess;
            this.expression = expression;
        }
    }
}
