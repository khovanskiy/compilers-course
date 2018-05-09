package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference;

import lombok.extern.slf4j.Slf4j;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.visitor.AbstractASTVisitor;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.*;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Slf4j
public class TypeInferencer extends AbstractASTVisitor<TypeInferenceContext> {
    public TypeContext inference(final AST.CompilationUnit compilationUnit) {
        final TypeInferenceContext inferenceContext = new TypeInferenceContext();
        visitCompilationUnit(compilationUnit, inferenceContext);
        return inferenceContext.getTypeContext();
    }

    @Override
    public void visitCompilationUnit(final AST.CompilationUnit compilationUnit, final TypeInferenceContext context) {
        final List<AST.SingleStatement> statements = compilationUnit.getCompoundStatement().getStatements();
        final List<AST.FunctionDefinition> functions = statements.stream()
                .filter(AST.FunctionDefinition.class::isInstance)
                .map(AST.FunctionDefinition.class::cast)
                .collect(Collectors.toList());
        final List<AST.SingleStatement> mainStatements = statements.stream().filter(s -> !AST.FunctionDefinition.class.isInstance(s))
                .collect(Collectors.toList());
        functions.add(new AST.FunctionDefinition("main", Collections.emptyList(), new AST.CompoundStatement(mainStatements)));

        initExternalFunctions(context);

        for (final AST.FunctionDefinition f : functions) {
            visitFunctionDefinition(f, context);
        }
        final TypeContext typeContext = new Evaluator(context, context.getRelations()).run();
        context.setTypeContext(typeContext);
    }

    @Override
    public void visitFunctionDefinition(final AST.FunctionDefinition functionDefinition, final TypeInferenceContext context) {
        context.wrapFunction(functionDefinition.getName(), scope -> {
            for (final AST.VariableDefinition variableDefinition : functionDefinition.getVariables()) {
                visitVariableDefinition(variableDefinition, context);
            }
            visitCompoundStatement(functionDefinition.getCompoundStatement(), context);
        });
    }

    @Override
    public void visitVariableDefinition(final AST.VariableDefinition variableDefinition, final TypeInferenceContext context) {
        final String name = variableDefinition.getName();
        final int id = context.getScope().getVariableId(name);
        if (log.isDebugEnabled()) {
            log.debug("Variable \"" + name + "\" is mapped to ID = " + id);
        }
    }

    @Override
    public void visitAssignmentStatement(final AST.AssignmentStatement assignmentStatement, final TypeInferenceContext context) {
        if (assignmentStatement.getMemoryAccess() instanceof AST.VariableAccessExpression) {
            final AST.VariableAccessExpression variableAccessExpression = (AST.VariableAccessExpression) assignmentStatement.getMemoryAccess();
            final String name = variableAccessExpression.getName();
            visitExpression(assignmentStatement.getExpression(), context);
            final Type rvalueType = context.getScope().popType();
            final int id = context.getScope().getVariableId(name);
            context.getScope().setVariableType(id, rvalueType);
            return;
        }
        if (assignmentStatement.getMemoryAccess() instanceof AST.ArrayAccessExpression) {
            final AST.ArrayAccessExpression arrayAccessExpression = (AST.ArrayAccessExpression) assignmentStatement.getMemoryAccess();
            visitExpression(arrayAccessExpression.getExpression(), context);
            final Type indexType = context.getScope().popType();
            visitExpression(assignmentStatement.getExpression(), context);
            final Type rvalueType = context.getScope().popType();
            if (log.isDebugEnabled()) {
                final Type arrayType = buildArrayType(assignmentStatement.getMemoryAccess(), rvalueType, context);
                log.debug("Array Type: " + arrayType);
            }
            context.addTypeRelation(indexType, IntegerType.INSTANCE);
            return;
        }
        throw new IllegalStateException();
    }

    /**
     * Builds the type of array with given element type
     *
     * @param memoryAccess the memory access
     * @param type         the element type
     * @param context      the type inference context
     * @return the built type of array
     */
    private Type buildArrayType(AST.MemoryAccessExpression memoryAccess, Type type, TypeInferenceContext context) {
        if (memoryAccess instanceof AST.VariableAccessExpression) {
            final AST.VariableAccessExpression variableAccessExpression = (AST.VariableAccessExpression) memoryAccess;
            final String name = variableAccessExpression.getName();
            final int id = context.getScope().getVariableId(name);
            context.getScope().setVariableType(id, type);
            return type;
        }
        if (memoryAccess instanceof AST.ArrayAccessExpression) {
            visitExpression(((AST.ArrayAccessExpression) memoryAccess).getExpression(), context);
            final Type indexType = context.getScope().popType();
            context.addTypeRelation(indexType, IntegerType.INSTANCE);
            return buildArrayType(((AST.ArrayAccessExpression) memoryAccess).getPointer(), ImplicationType.of(ArrayType.INSTANCE, type), context);
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitIfStatement(final AST.IfStatement ifStatement, final TypeInferenceContext context) {
        final List<AST.IfCase> cases = ifStatement.getCases();
        for (int i = 0; i < cases.size(); ++i) {
            final AST.IfCase ifCase = cases.get(i);
            if (ifCase.getCondition() == null) {
                // else
                assert i == cases.size() - 1;
                visitCompoundStatement(ifCase.getCompoundStatement(), context);
            } else {
                // if or elif
                visitExpression(ifCase.getCondition(), context);
                visitCompoundStatement(ifCase.getCompoundStatement(), context);
            }
        }
    }

    @Override
    public void visitGotoStatement(final AST.GotoStatement gotoStatement, final TypeInferenceContext context) {
        // do nothing
    }

    @Override
    public void visitLabelStatement(AST.LabelStatement labelStatement, TypeInferenceContext typeInferenceContext) {
        // do nothing
    }

    @Override
    public void visitContinueStatement(final AST.ContinueStatement continueStatement, final TypeInferenceContext context) {
        // do nothing
    }

    @Override
    public void visitBreakStatement(final AST.BreakStatement breakStatement, final TypeInferenceContext context) {
        // do nothing
    }

    @Override
    public void visitReturnStatement(final AST.ReturnStatement returnStatement, final TypeInferenceContext context) {
        if (returnStatement.getExpression() != null) {
            visitExpression(returnStatement.getExpression(), context);
            final Type returnType = context.getScope().popType();
            context.getScope().setReturnType(returnType);
        } else {
            context.getScope().setReturnType(VoidType.INSTANCE);
        }
    }

    @Override
    public void visitSkipStatement(final AST.SkipStatement skipStatement, final TypeInferenceContext context) {
        // do nothing
    }

    @Override
    public void visitWhileStatement(final AST.WhileStatement whileStatement, final TypeInferenceContext context) {
        visitExpression(whileStatement.getCondition(), context);
        visitCompoundStatement(whileStatement.getCompoundStatement(), context);
    }

    @Override
    public void visitRepeatStatement(final AST.RepeatStatement repeatStatement, final TypeInferenceContext context) {
        visitExpression(repeatStatement.getCondition(), context);
        visitCompoundStatement(repeatStatement.getCompoundStatement(), context);
    }

    @Override
    public void visitForStatement(final AST.ForStatement forStatement, final TypeInferenceContext context) {
        if (forStatement.getInit() != null) {
            visitAssignmentStatement(forStatement.getInit(), context);
        }
        if (forStatement.getCondition() != null) {
            visitExpression(forStatement.getCondition(), context);
        }
        visitCompoundStatement(forStatement.getCompoundStatement(), context);
        if (forStatement.getLoop() != null) {
            visitAssignmentStatement(forStatement.getLoop(), context);
        }
    }

    @Override
    public void visitFunctionCall(final AST.FunctionCall functionCall, final TypeInferenceContext context) {
        final TypeInferenceContext.Scope functionScope = context.getScopeByName(functionCall.getName());
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
    public void visitArrayCreation(final AST.ArrayCreationExpression arrayCreationExpression, final TypeInferenceContext context) {
        Type elementType = null;
        for (final AST.Expression expression : arrayCreationExpression.getArguments()) {
            visitExpression(expression, context);
            final Type argumentType = context.getScope().popType();
            if (elementType == null) {
                elementType = argumentType;
            } else if (!elementType.equals(argumentType)) {
                throw new IllegalStateException();
            }
        }
        if (elementType == null) {
            elementType = context.getScope().createTypeVariable();
        }
        context.getScope().pushType(ImplicationType.of(ArrayType.INSTANCE, elementType));
    }

    @Override
    public void visitVariableAccessForRead(final AST.VariableAccessExpression variableAccessExpression, final TypeInferenceContext context) {
        final String name = variableAccessExpression.getName();
        final int id = context.getScope().getVariableId(name);
        final Type variableType = context.getScope().getVariableType(id);
        context.getScope().pushType(variableType);
    }

    @Override
    public void visitArrayAccessForRead(final AST.ArrayAccessExpression arrayAccessExpression, final TypeInferenceContext context) {
        visitMemoryAccessForRead(arrayAccessExpression.getPointer(), context);
        final Type arrayType = context.getScope().popType();
        visitExpression(arrayAccessExpression.getExpression(), context);
        final Type indexType = context.getScope().popType();
        context.addTypeRelation(indexType, IntegerType.INSTANCE);
        final Type elementType = context.getScope().createTypeVariable();
        context.getScope().pushType(ApplicationType.of(arrayType, elementType));
    }

    @Override
    public void visitVariableAccessForWrite(final AST.VariableAccessExpression variableAccessExpression, final TypeInferenceContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitArrayAccessForWrite(final AST.ArrayAccessExpression arrayAccessExpression, final TypeInferenceContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitIntegerLiteral(final AST.IntegerLiteral integerLiteral, final TypeInferenceContext context) {
        context.getScope().pushType(IntegerType.INSTANCE);
    }

    @Override
    public void visitCharacterLiteral(final AST.CharacterLiteral characterLiteral, final TypeInferenceContext context) {
        context.getScope().pushType(CharacterType.INSTANCE);
    }

    @Override
    public void visitStringLiteral(final AST.StringLiteral stringLiteral, final TypeInferenceContext context) {
        context.getScope().pushType(ImplicationType.of(ArrayType.INSTANCE, CharacterType.INSTANCE));
    }

    @Override
    public void visitNullLiteral(final AST.NullLiteral nullLiteral, final TypeInferenceContext context) {
        context.getScope().pushType(context.getScope().createTypeVariable());
    }

    @Override
    public void visitUnaryExpression(final AST.UnaryExpression unaryExpression, final TypeInferenceContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitBinaryExpression(final AST.BinaryExpression binaryExpression, final TypeInferenceContext context) {
        visitExpression(binaryExpression.getLeft(), context);
        final Type leftType = context.getScope().popType();
        visitExpression(binaryExpression.getRight(), context);
        final Type rightType = context.getScope().popType();

        final String operator = binaryExpression.getOperator();
        if (operator.equals("==") || operator.equals("!=")) {
            context.addTypeRelation(leftType, rightType);
        } else {
            context.addTypeRelation(leftType, IntegerType.INSTANCE);
            context.addTypeRelation(rightType, IntegerType.INSTANCE);
        }

        final ConcreteType resultType = IntegerType.INSTANCE;
        context.getScope().pushType(resultType);
    }

    private void initExternalFunctions(final TypeInferenceContext context) {
        context.wrapFunction("read", scope -> {
            scope.setReturnType(IntegerType.INSTANCE);
        });
        context.wrapFunction("write", scope -> {
            scope.setVariableType(0, IntegerType.INSTANCE, true);
            scope.setReturnType(VoidType.INSTANCE);
        });
        context.wrapFunction("strlen", scope -> {
            scope.setVariableType(0, ImplicationType.of(ArrayType.INSTANCE, CharacterType.INSTANCE));
            scope.setReturnType(IntegerType.INSTANCE);
        });
        context.wrapFunction("strget", scope -> {
            scope.setVariableType(0, ImplicationType.of(ArrayType.INSTANCE, CharacterType.INSTANCE));
            scope.setVariableType(1, IntegerType.INSTANCE);
            scope.setReturnType(CharacterType.INSTANCE);
        });
        context.wrapFunction("strset", scope -> {
            scope.setVariableType(0, ImplicationType.of(ArrayType.INSTANCE, CharacterType.INSTANCE));
            scope.setVariableType(1, IntegerType.INSTANCE);
            scope.setVariableType(2, CharacterType.INSTANCE);
            scope.setReturnType(CharacterType.INSTANCE);
        });
        context.wrapFunction("strsub", scope -> {
            scope.setVariableType(0, ImplicationType.of(ArrayType.INSTANCE, CharacterType.INSTANCE));
            scope.setVariableType(1, IntegerType.INSTANCE);
            scope.setVariableType(2, IntegerType.INSTANCE);
            scope.setReturnType(ImplicationType.of(ArrayType.INSTANCE, CharacterType.INSTANCE));
        });
        context.wrapFunction("strdup", scope -> {
            scope.setVariableType(0, ImplicationType.of(ArrayType.INSTANCE, CharacterType.INSTANCE));
            scope.setReturnType(ImplicationType.of(ArrayType.INSTANCE, CharacterType.INSTANCE));
        });
        context.wrapFunction("strcat", scope -> {
            scope.setVariableType(0, ImplicationType.of(ArrayType.INSTANCE, CharacterType.INSTANCE));
            scope.setVariableType(1, ImplicationType.of(ArrayType.INSTANCE, CharacterType.INSTANCE));
            scope.setReturnType(ImplicationType.of(ArrayType.INSTANCE, CharacterType.INSTANCE));
        });
        context.wrapFunction("strcmp", scope -> {
            scope.setVariableType(0, ImplicationType.of(ArrayType.INSTANCE, CharacterType.INSTANCE));
            scope.setVariableType(1, ImplicationType.of(ArrayType.INSTANCE, CharacterType.INSTANCE));
            scope.setReturnType(IntegerType.INSTANCE);
        });
        context.wrapFunction("strmake", scope -> {
            scope.setVariableType(0, IntegerType.INSTANCE);
            scope.setVariableType(1, CharacterType.INSTANCE);
            scope.setReturnType(ImplicationType.of(ArrayType.INSTANCE, CharacterType.INSTANCE));
        });
        context.wrapFunction("arrlen", scope -> {
            scope.setVariableType(0, ImplicationType.of(ArrayType.INSTANCE, scope.createTypeVariable()), true);
            scope.setReturnType(IntegerType.INSTANCE);
        });
        context.wrapFunction("arrmake", scope -> {
            scope.setVariableType(0, IntegerType.INSTANCE);
            scope.setVariableType(1, IntegerType.INSTANCE);
            scope.setReturnType(ImplicationType.of(ArrayType.INSTANCE, IntegerType.INSTANCE));
        });
        context.wrapFunction("Arrmake", scope -> {
            scope.setVariableType(0, IntegerType.INSTANCE);
            scope.setVariableType(1, IntegerType.INSTANCE);
            scope.setReturnType(ImplicationType.of(ArrayType.INSTANCE, ImplicationType.of(ArrayType.INSTANCE, IntegerType.INSTANCE)));
        });
    }

    public class Evaluator {
        private final TypeInferenceContext context;

        private final Set<TypeRelation> relations = new HashSet<>();

        private final Map<TypeVariable, ConcreteType> types = new HashMap<>();

        public Evaluator(final TypeInferenceContext context, final List<TypeRelation> relations) {
            this.context = context;
            this.relations.addAll(relations);
        }

        public TypeContext run() {
//            System.out.println("Relations");
//            relations.forEach(rel -> {
//                System.out.println(rel);
//            });
            final Set<TypeRelation> toAdd = new LinkedHashSet<>();
            boolean updated;
            do {
                boolean updatedConcreteTypes = false;
                for (final TypeRelation rel : relations) {
                    if (rel.getLeft() instanceof TypeVariable && !((TypeVariable) rel.getLeft()).isIgnore()) {
                        updatedConcreteTypes |= updateTransitiveRelations(toAdd, (TypeVariable) rel.getLeft(), rel.getRight());
                    }
                    if (rel.getRight() instanceof TypeVariable && !((TypeVariable) rel.getRight()).isIgnore()) {
                        updatedConcreteTypes |= updateTransitiveRelations(toAdd, (TypeVariable) rel.getRight(), rel.getLeft());
                    }
                    if (rel.getLeft().isConcreteType() && rel.getRight().isConcreteType() && !rel.getLeft().equals(rel.getRight())) {
                        throw new IllegalStateException(rel.getLeft() + " is not equal to " + rel.getRight());
                    }
                }
                updated = relations.addAll(toAdd);
                if (log.isTraceEnabled()) {
                    log.trace("Added " + toAdd.size() + " new relations");
                }
                if (updatedConcreteTypes) {
                    boolean foundAll = true;
                    for (final Map.Entry<String, TypeInferenceContext.Scope> entry : context.getFunctionScopes().entrySet()) {
                        final String functionName = entry.getKey();
                        final TypeInferenceContext.Scope scope = entry.getValue();
                        for (final TypeVariable t : scope.getVariables().values()) {
                            if (types.get(t) == null) {
                                if (log.isTraceEnabled()) {
                                    log.trace("Unknown type for " + t);
                                }
                                foundAll = false;
                            }
                        }
                    }
                    if (foundAll) {
                        if (log.isInfoEnabled()) {
                            log.info("All types are inferenced");
                        }
                        break;
                    }
                }
                toAdd.clear();
            } while (updated);
//            System.out.println("=====\nNew Relations");
//            relations.forEach(rel -> {
//                System.out.println(rel);
//            });
            final TypeContext typeContext = new TypeContext();
            types.forEach((tv, t) -> {
                final TypeContext.Scope functionScope = typeContext.getScopeByName(tv.getFunctionName());
                if (tv.getVariableId() >= 0) {
                    functionScope.setVariableType(tv.getVariableId(), t);
                } else {
                    functionScope.setReturnType(t);
                }
                if (log.isTraceEnabled()) {
                    log.trace("Type of " + tv + " is " + t);
                }
            });
            return typeContext;
        }

        private void addRelation(final Set<TypeRelation> set, final TypeRelation rel) {
            if (rel.getLeft().equals(rel.getRight())) {
                return;
            }
            if (!relations.contains(rel)) {
                set.add(rel);
            }
        }

        private boolean updateTransitiveRelations(final Set<TypeRelation> toAdd, final TypeVariable typeVariable, final Type type) {
            for (final TypeRelation ab : relations) {
                if (ab.getLeft().equals(typeVariable)) {
                    addRelation(toAdd, new TypeRelation(type, ab.getRight()));
                } else if (ab.getLeft().getTypeVariables().contains(typeVariable)) {
                    addRelation(toAdd, new TypeRelation(ab.getLeft().substitute(typeVariable, type), ab.getRight()));
                }
                if (ab.getRight().equals(type)) {
                    addRelation(toAdd, new TypeRelation(ab.getLeft(), type));
                } else if (ab.getRight().getTypeVariables().contains(typeVariable)) {
                    addRelation(toAdd, new TypeRelation(ab.getLeft(), ab.getRight().substitute(typeVariable, type)));
                }
            }
            return type.isConcreteType() && types.put(typeVariable, (ConcreteType) type) == null;
        }
    }
}
