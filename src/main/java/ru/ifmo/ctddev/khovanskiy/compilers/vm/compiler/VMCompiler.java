package ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler;

import lombok.extern.slf4j.Slf4j;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.visitor.AbstractASTVisitor;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.TypeContext;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract syntax tree compiler to the virtual machine code
 *
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Slf4j
public class VMCompiler extends AbstractASTVisitor<VMCompilerContext> {
    /**
     * Compiles the abstract syntax tree to the virtual machine code
     *
     * @param ast         the ast of program
     * @param typeContext the type context of the program
     * @since 1.0.0
     */
    public VMProgram compile(final AST.CompilationUnit ast, final TypeContext typeContext) {
        final VMCompilerContext program = new VMCompilerContext(typeContext);
        visitCompilationUnit(ast, program);
        return program.getVmProgram();
    }

    @Override
    public void visitCompilationUnit(final AST.CompilationUnit compilationUnit, final VMCompilerContext context) {
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
    public void visitFunctionDefinition(final AST.FunctionDefinition functionDefinition, final VMCompilerContext context) {
        context.wrapFunction(functionDefinition.getName(), scope -> {
            final TypeContext.Scope typeScope = context.getTypeContext().getScopeByName(scope.getName());
            final List<ConcreteType> types = typeScope.getVariableTypes();
            final ConcreteType returnType = typeScope.getReturnType();
            context.registerFunction(functionDefinition.getName(), functionDefinition.getVariables().size(), types, returnType);

            for (int i = 0; i < functionDefinition.getVariables().size(); ++i) {
                final AST.VariableDefinition variableDefinition = functionDefinition.getVariables().get(i);
                visitVariableDefinition(variableDefinition, context);
            }
            visitCompoundStatement(functionDefinition.getCompoundStatement(), context);
        });
    }

    @Override
    public void visitVariableDefinition(final AST.VariableDefinition variableDefinition, final VMCompilerContext context) {
        final String name = variableDefinition.getName();
        final int id = context.getScope().rename(name);
        log.info("Function {}: rename variable {} to {}", context.getScope().getName(), name, id);
    }

    @Override
    public void visitAssignmentStatement(final AST.AssignmentStatement assignmentStatement, final VMCompilerContext context) {
        if (assignmentStatement.getMemoryAccess() instanceof AST.VariableAccessExpression) {
            final AST.VariableAccessExpression variableAccessExpression = (AST.VariableAccessExpression) assignmentStatement.getMemoryAccess();
            final String name = variableAccessExpression.getName();
            final int id = context.getScope().rename(name);
            // expression
            visitExpression(assignmentStatement.getExpression(), context);
            //
            final ConcreteType type = context.getTypeContext().getScopeByName(context.getScope().getName()).getVariableType(id);
            if (type.equals(IntegerType.INSTANCE)) {
                context.addCommand(new VM.IStore(id));
            } else if (type.equals(CharacterType.INSTANCE)) {
                context.addCommand(new VM.IStore(id));
            } else {
                context.addCommand(new VM.AStore(id));
            }
        } else {
            final AST.ArrayAccessExpression arrayAccessExpression = (AST.ArrayAccessExpression) assignmentStatement.getMemoryAccess();
            visitMemoryAccessForRead(arrayAccessExpression.getPointer(), context);
            visitExpression(arrayAccessExpression.getExpression(), context);
            // expression
            visitExpression(assignmentStatement.getExpression(), context);
            //
            final Type elementType = getArrayType(arrayAccessExpression.getPointer(), context).getRight();
            if (elementType.equals(IntegerType.INSTANCE)) {
                context.addCommand(new VM.IAStore());
            } else if (elementType.equals(CharacterType.INSTANCE)) {
                context.addCommand(new VM.IAStore());
            } else {
                context.addCommand(new VM.AAStore());
            }
        }
    }

    private ImplicationType getArrayType(final AST.MemoryAccessExpression memoryAccess, final VMCompilerContext context) {
        if (memoryAccess instanceof AST.VariableAccessExpression) {
            final String name = ((AST.VariableAccessExpression) memoryAccess).getName();
            final int id = context.getScope().rename(name);
            return (ImplicationType) context.getTypeContext().getScopeByName(context.getScope().getName()).getVariableType(id);
        }
        if (memoryAccess instanceof AST.ArrayAccessExpression) {
            ConcreteType concreteType = getArrayType(((AST.ArrayAccessExpression) memoryAccess).getPointer(), context);
            assert ImplicationType.class.isInstance(concreteType);
            return (ImplicationType) ((ImplicationType) concreteType).getRight();
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitIfStatement(final AST.IfStatement ifStatement, final VMCompilerContext context) {
        final List<AST.IfCase> cases = ifStatement.getCases();
        final List<String> labels = new ArrayList<>(cases.size());
        for (int i = 0; i < cases.size(); ++i) {
            labels.add(context.getNextLabel());
        }
        final String endLabel = context.getNextLabel();
        labels.add(endLabel); // end label
        for (int i = 0; i < cases.size(); ++i) {
            final AST.IfCase ifCase = cases.get(i);
            context.addCommand(new VM.Label(labels.get(i)));
            if (ifCase.getCondition() == null) {
                // else
                assert i == cases.size() - 1;
                visitCompoundStatement(ifCase.getCompoundStatement(), context);
            } else {
                // if or elif
                visitExpression(ifCase.getCondition(), context);
                context.addCommand(new VM.IfFalse(labels.get(i + 1)));
                visitCompoundStatement(ifCase.getCompoundStatement(), context);
                context.addCommand(new VM.Goto(endLabel));
            }
        }
        context.addCommand(new VM.Label(endLabel));
    }

    @Override
    public void visitGotoStatement(final AST.GotoStatement gotoStatement, final VMCompilerContext context) {
        context.addCommand(new VM.Goto(gotoStatement.getLabel()));
    }

    @Override
    public void visitLabelStatement(AST.LabelStatement labelStatement, VMCompilerContext context) {
        context.addCommand(new VM.Label(labelStatement.getName()));
    }

    @Override
    public void visitContinueStatement(final AST.ContinueStatement continueStatement, final VMCompilerContext context) {
        context.addCommand(new VM.Goto(context.getLoop().getLoopLabel()));
    }

    @Override
    public void visitBreakStatement(final AST.BreakStatement breakStatement, final VMCompilerContext context) {
        context.addCommand(new VM.Goto(context.getLoop().getEndLabel()));
    }

    @Override
    public void visitReturnStatement(final AST.ReturnStatement returnStatement, final VMCompilerContext context) {
        if (returnStatement.getExpression() != null) {
            visitExpression(returnStatement.getExpression(), context);
        }
        final ConcreteType returnType = context.getTypeContext().getScopeByName(context.getScope().getName()).getReturnType();
        if (returnType.equals(VoidType.INSTANCE)) {
            context.addCommand(new VM.Return());
        } else if (returnType.equals(IntegerType.INSTANCE)) {
            context.addCommand(new VM.IReturn());
        } else if (returnType.equals(CharacterType.INSTANCE)) {
            context.addCommand(new VM.IReturn());
        } else {
            context.addCommand(new VM.AReturn());
        }
    }

    @Override
    public void visitSkipStatement(final AST.SkipStatement skipStatement, final VMCompilerContext context) {
        // do nothing
    }

    @Override
    public void visitWhileStatement(final AST.WhileStatement whileStatement, final VMCompilerContext context) {
        final String loopLabel = context.getNextLabel();
        final String endLabel = context.getNextLabel();
        visitExpression(whileStatement.getCondition(), context);
        context.addCommand(new VM.IfFalse(endLabel));
        context.addCommand(new VM.Label(loopLabel));
        context.wrapLoop(loopLabel, endLabel, loop -> {
            visitCompoundStatement(whileStatement.getCompoundStatement(), context);
            visitExpression(whileStatement.getCondition(), context);
            context.addCommand(new VM.IfTrue(loopLabel));
        });
        context.addCommand(new VM.Label(endLabel));
    }

    @Override
    public void visitRepeatStatement(final AST.RepeatStatement repeatStatement, final VMCompilerContext context) {
        final String loopLabel = context.getNextLabel();
        final String endLabel = context.getNextLabel();
        context.addCommand(new VM.Label(loopLabel));
        context.wrapLoop(loopLabel, endLabel, loop -> {
            visitCompoundStatement(repeatStatement.getCompoundStatement(), context);
            visitExpression(repeatStatement.getCondition(), context);
            context.addCommand(new VM.IfFalse(loopLabel));
        });
        context.addCommand(new VM.Label(endLabel));
    }

    @Override
    public void visitForStatement(final AST.ForStatement forStatement, final VMCompilerContext context) {
        if (forStatement.getInit() != null) {
            visitAssignmentStatement(forStatement.getInit(), context);
        }
        final String loopLabel = context.getNextLabel();
        final String endLabel = context.getNextLabel();
        final String continueLabel = context.getNextLabel();
        context.addCommand(new VM.Label(loopLabel));
        context.wrapLoop(continueLabel, endLabel, loop -> {
            if (forStatement.getCondition() != null) {
                visitExpression(forStatement.getCondition(), context);
                context.addCommand(new VM.IfFalse(endLabel));
            }
            visitCompoundStatement(forStatement.getCompoundStatement(), context);
            context.addCommand(new VM.Label(continueLabel));
            if (forStatement.getLoop() != null) {
                visitAssignmentStatement(forStatement.getLoop(), context);
            }
            context.addCommand(new VM.Goto(loopLabel));
        });
        context.addCommand(new VM.Label(endLabel));
    }

    @Override
    public void visitFunctionCall(final AST.FunctionCall functionCall, final VMCompilerContext context) {
        final List<AST.Expression> arguments = functionCall.getArguments();
        for (int i = 0; i < arguments.size(); ++i) {
            final AST.Expression expression = arguments.get(i);
            visitExpression(expression, context);
        }
        final ConcreteType returnType = context.getTypeContext().getScopeByName(functionCall.getName()).getReturnType();
        context.addCommand(new VM.InvokeStatic(functionCall.getName(), functionCall.getArguments().size(), returnType));
    }

    @Override
    public void visitArrayCreation(final AST.ArrayCreationExpression arrayCreationExpression, final VMCompilerContext context) {
        final List<AST.Expression> arguments = arrayCreationExpression.getArguments();
        final int length = arguments.size();
        context.addCommand(new VM.IConst(length));
        context.addCommand(new VM.NewArray());
        for (int i = 0; i < length; ++i) {
            context.addCommand(new VM.Dup());
            context.addCommand(new VM.IConst(i));
            visitExpression(arguments.get(i), context);
            context.addCommand(new VM.IAStore());
        }
    }

    @Override
    public void visitVariableAccessForRead(final AST.VariableAccessExpression variableAccessExpression, final VMCompilerContext context) {
        final String name = variableAccessExpression.getName();
        if ("true".equals(name)) {
            context.addCommand(new VM.IConst(1));
        } else if ("false".equals(name)) {
            context.addCommand(new VM.IConst(0));
        } else {
            final int id = context.getScope().rename(name);
            final ConcreteType type = context.getTypeContext().getScopeByName(context.getScope().getName()).getVariableType(id);
            if (type.equals(IntegerType.INSTANCE)) {
                context.addCommand(new VM.ILoad(id));
            } else if (type.equals(CharacterType.INSTANCE)) {
                context.addCommand(new VM.ILoad(id));
            } else {
                context.addCommand(new VM.ALoad(id));
            }
        }
    }

    @Override
    public void visitArrayAccessForRead(final AST.ArrayAccessExpression arrayAccessExpression, final VMCompilerContext context) {
        visitMemoryAccessForRead(arrayAccessExpression.getPointer(), context);
        visitExpression(arrayAccessExpression.getExpression(), context);
        final Type elementType = getArrayType(arrayAccessExpression.getPointer(), context).getRight();
        if (elementType.equals(IntegerType.INSTANCE)) {
            context.addCommand(new VM.IALoad());
        } else if (elementType.equals(CharacterType.INSTANCE)) {
            context.addCommand(new VM.IALoad());
        } else {
            context.addCommand(new VM.AALoad());
        }
    }

    @Override
    public void visitVariableAccessForWrite(final AST.VariableAccessExpression variableAccessExpression, final VMCompilerContext context) {
        visitVariableAccessForRead(variableAccessExpression, context);
    }

    @Override
    public void visitArrayAccessForWrite(final AST.ArrayAccessExpression arrayAccessExpression, final VMCompilerContext context) {
        visitArrayAccessForRead(arrayAccessExpression, context);
    }

    @Override
    public void visitIntegerLiteral(final AST.IntegerLiteral integerLiteral, final VMCompilerContext context) {
        context.addCommand(new VM.IConst(integerLiteral.getValue()));
    }

    @Override
    public void visitCharacterLiteral(final AST.CharacterLiteral characterLiteral, final VMCompilerContext context) {
        final char character = characterLiteral.getValue();
        context.addCommand(new VM.IConst(character));
    }

    @Override
    public void visitStringLiteral(final AST.StringLiteral stringLiteral, final VMCompilerContext context) {
        final String string = stringLiteral.getValue();
        final int length = string.length();
        context.addCommand(new VM.IConst(length));
        context.addCommand(new VM.NewArray());
        for (int i = 0; i < length; ++i) {
            context.addCommand(new VM.Dup());
            context.addCommand(new VM.IConst(i));
            context.addCommand(new VM.IConst(string.charAt(i)));
            context.addCommand(new VM.IAStore());
        }
    }

    @Override
    public void visitNullLiteral(final AST.NullLiteral nullLiteral, final VMCompilerContext context) {
        context.addCommand(new VM.AConstNull());
    }

    @Override
    public void visitUnaryExpression(final AST.UnaryExpression unaryExpression, final VMCompilerContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitBinaryExpression(final AST.BinaryExpression binaryExpression, final VMCompilerContext context) {
        visitExpression(binaryExpression.getLeft(), context);
        visitExpression(binaryExpression.getRight(), context);
        context.addCommand(new VM.BinOp(binaryExpression.getOperator()));
    }
}
