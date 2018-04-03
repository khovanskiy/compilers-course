package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference;

import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.visitor.AbstractASTVisitor;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class TypeInferencer extends AbstractASTVisitor<Context> {
    @Override
    public void visitCompilationUnit(final AST.CompilationUnit compilationUnit, final Context context) throws Exception {
        final List<AST.SingleStatement> statements = compilationUnit.getCompoundStatement().getStatements();
        final List<AST.FunctionDefinition> functions = statements.stream()
                .filter(AST.FunctionDefinition.class::isInstance)
                .map(AST.FunctionDefinition.class::cast)
                .collect(Collectors.toList());
        final List<AST.SingleStatement> mainStatements = statements.stream().filter(s -> !AST.FunctionDefinition.class.isInstance(s))
                .collect(Collectors.toList());
        functions.add(new AST.FunctionDefinition("main", Collections.emptyList(), new AST.CompoundStatement(mainStatements)));
        for (final AST.FunctionDefinition f : functions) {
            visitFunctionDefinition(f, context);
        }
    }

    @Override
    public void visitFunctionDefinition(final AST.FunctionDefinition functionDefinition, final Context context) throws Exception {
        context.wrapFunction(functionDefinition.getName(), scope -> {
            for (final AST.VariableDefinition variableDefinition : functionDefinition.getVariables()) {
                visitVariableDefinition(variableDefinition, context);
            }
            visitCompoundStatement(functionDefinition.getCompoundStatement(), context);

//            scope.getTypes().forEach((id, type) -> System.out.println("Variable v" + id + " has type " + type));
        });
        context.getRelations().forEach(typeRelation -> {
            System.out.println(typeRelation);
        });
        new Evaluator(context.getRelations()).run();
    }

    @Override
    public void visitVariableDefinition(final AST.VariableDefinition variableDefinition, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitAssignmentStatement(final AST.AssignmentStatement assignmentStatement, final Context context) throws Exception {
        if (assignmentStatement.getMemoryAccess() instanceof AST.VariableAccessExpression) {
            final AST.VariableAccessExpression variableAccessExpression = (AST.VariableAccessExpression) assignmentStatement.getMemoryAccess();
            final String name = variableAccessExpression.getName();
            visitExpression(assignmentStatement.getExpression(), context);
            final Type rvalueType = context.getScope().popType();
            final int id = context.getScope().getVariableId(name);
            context.getScope().setVariableType(id, rvalueType);
            return;
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitIfStatement(final AST.IfStatement ifStatement, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitGotoStatement(final AST.GotoStatement gotoStatement, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitContinueStatement(final AST.ContinueStatement continueStatement, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitBreakStatement(final AST.BreakStatement breakStatement, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitReturnStatement(final AST.ReturnStatement returnStatement, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitSkipStatement(final AST.SkipStatement skipStatement, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitWhileStatement(final AST.WhileStatement whileStatement, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitRepeatStatement(final AST.RepeatStatement repeatStatement, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitForStatement(final AST.ForStatement forStatement, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitFunctionCall(final AST.FunctionCall functionCall, final Context context) throws Exception {
        final Context.Scope functionScope = context.getScopeByName(functionCall.getName());
        final List<AST.Expression> arguments = functionCall.getArguments();
        for (int i = 0; i < arguments.size(); ++i) {
            final AST.Expression expression = arguments.get(i);
            visitExpression(expression, context);
            final Type argumentType = context.getScope().popType();
            functionScope.setVariableType(i, argumentType);
        }
        final Type functionReturnType = functionScope.getReturnType();
        context.getScope().pushType(functionReturnType);
    }

    @Override
    public void visitArrayCreation(final AST.ArrayCreationExpression arrayCreationExpression, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitVariableAccess(final AST.VariableAccessExpression variableAccessExpression, final Context context) throws Exception {
        final String name = variableAccessExpression.getName();
        final int id = context.getScope().getVariableId(name);
        final Type variableType = context.getScope().getVariableType(id);
        context.getScope().pushType(variableType);
    }

    @Override
    public void visitArrayAccess(final AST.ArrayAccessExpression arrayAccessExpression, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitVariableAccessForWrite(final AST.VariableAccessExpression variableAccessExpression, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitArrayAccessForWrite(final AST.ArrayAccessExpression arrayAccessExpression, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitIntegerLiteral(final AST.IntegerLiteral integerLiteral, final Context context) throws Exception {
        context.getScope().pushType(new ConcreteType(ConcreteType.VALUE.INTEGER));
    }

    @Override
    public void visitCharacterLiteral(final AST.CharacterLiteral characterLiteral, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitStringLiteral(final AST.StringLiteral stringLiteral, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitNullLiteral(final AST.NullLiteral nullLiteral, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitUnaryExpression(final AST.UnaryExpression unaryExpression, final Context context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitBinaryExpression(final AST.BinaryExpression binaryExpression, final Context context) throws Exception {
        visitExpression(binaryExpression.getLeft(), context);
        final Type leftType = context.getScope().popType();
        visitExpression(binaryExpression.getRight(), context);
        final Type rightType = context.getScope().popType();
        final ConcreteType resultType = new ConcreteType(ConcreteType.VALUE.INTEGER);
        context.addTypeRelation(leftType, resultType);
        context.addTypeRelation(rightType, resultType);
        context.getScope().pushType(resultType);
    }

    public static class Evaluator {
        private final Map<TypeVariable, Set<Type>> upperBounds = new HashMap<>();

        private final Map<TypeVariable, Set<Type>> lowerBounds = new HashMap<>();

        private final List<TypeRelation> relations;

        public Evaluator(List<TypeRelation> relations) {
            this.relations = relations;
        }

        /**
         * SubC is the structural decomposition function, which in this simple case will just return a singleton list containing a new TypeRelation of its parameters.
         */
        public List<TypeRelation> subC(final Type left, final Type right) {
            return Collections.singletonList(new TypeRelation(left, right));
        }

        /**
         * https://stackoverflow.com/questions/6783463/hindley-milner-algorithm-in-java
         */
        public void run() {
            final Queue<TypeRelation> queue = new ArrayDeque<>();
            queue.addAll(relations);
            final List<TypeRelation> reflexives = new ArrayList<>();
            while (!queue.isEmpty()) {
                final TypeRelation rel = queue.poll();
                if (rel.getLeft() instanceof TypeVariable && rel.getRight() instanceof TypeVariable) {
                    final TypeVariable relLeft = (TypeVariable) rel.getLeft();
                    final TypeVariable relRight = (TypeVariable) rel.getRight();
                    // case 1
                    boolean found1 = false;
                    boolean found2 = false;
                    for (final TypeRelation ab : reflexives) {
                        if (ab.getRight().equals(relLeft)) {
                            final TypeVariable abLeft = (TypeVariable) ab.getLeft();
                            found1 = true;
                            // add (ab.left, rel.right) to Reflexives
                            reflexives.add(new TypeRelation(abLeft, relRight));
                            // union and set upper bounds of ab.left with upper bounds of rel.right
                            final Set<Type> union = SetUtils.union(getUpperBounds(abLeft), getUpperBounds(relRight));
                            upperBounds.put(abLeft, union);
                        }
                        if (ab.getLeft().equals(relRight)) {
                            final TypeVariable abRight = (TypeVariable) ab.getRight();
                            found2 = true;
                            // add (rel.left, ab.right) to Reflexives
                            reflexives.add(new TypeRelation(relLeft, abRight));
                            // intersect and set lower bounds of ab.right  with lower bounds of rel.left
                            final Set<Type> intersection = SetUtils.intersection(getLowerBounds(abRight), getLowerBounds(relLeft));
                            lowerBounds.put(abRight, intersection);
                        }
                    }
                    if (!found1) {
                        // union and set upper bounds of rel.left with upper bounds of rel.right
                        final Set<Type> union = SetUtils.union(getUpperBounds(relLeft), getUpperBounds(relRight));
                        upperBounds.put(relLeft, union);
                    }
                    if (!found2) {
                        // intersect and set lower bounds of rel.right with lower bounds of rel.left
                        final Set<Type> intersection = SetUtils.intersection(getLowerBounds(relRight), getLowerBounds(relLeft));
                        lowerBounds.put(relRight, intersection);
                    }
                    // add TypeRelation(rel.left, rel.right) to Reflexives
                    reflexives.add(new TypeRelation(relLeft, relRight));

                    for (final Type lb : getLowerBounds(relLeft)) {
                        for (final Type ub : getUpperBounds(relRight)) {
                            // add all SubC(lb, ub) to TypeRelations
                            final List<TypeRelation> decomposition = subC(lb, ub);
                            relations.addAll(decomposition);
                        }
                    }
                    continue;
                }
                if (rel.getLeft() instanceof TypeVariable && rel.getRight() instanceof ConcreteType) {
                    // case 2
                    throw new UnsupportedOperationException();
                }
                if (rel.getLeft() instanceof ConcreteType && rel.getRight() instanceof TypeVariable) {
                    final TypeVariable right = (TypeVariable) rel.getRight();
                    if (!getLowerBounds(right).contains(rel.getLeft())) {
                        // case 3
                        boolean found = false;
                        for (final TypeRelation ab : reflexives) {
                            if (ab.getLeft().equals(rel.getRight())) {
                                found = true;

                            }
                        }
                        if (!found) {

                        }
                        continue;
                    }
                }
            }
        }

        public Set<Type> getLowerBounds(final TypeVariable typeVariable) {
            return lowerBounds.computeIfAbsent(typeVariable, k -> new HashSet<>());
        }

        public Set<Type> getUpperBounds(final TypeVariable typeVariable) {
            return upperBounds.computeIfAbsent(typeVariable, k -> new HashSet<>());
        }
    }
}
