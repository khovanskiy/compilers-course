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


@Slf4j
public class VMCompiler extends AbstractASTVisitor<CompilerContext> {
    public VMProgram compile(final AST.CompilationUnit ast, final TypeContext typeContext) throws Exception {
        final CompilerContext program = new CompilerContext(typeContext);
        visitCompilationUnit(ast, program);
        return program.getVmProgram();
    }

    @Override
    public void visitCompilationUnit(final AST.CompilationUnit compilationUnit, final CompilerContext compilerContext) throws Exception {
        final List<AST.SingleStatement> statements = compilationUnit.getCompoundStatement().getStatements();
        final List<AST.FunctionDefinition> functions = statements.stream()
                .filter(AST.FunctionDefinition.class::isInstance)
                .map(AST.FunctionDefinition.class::cast)
                .collect(Collectors.toList());
        final List<AST.SingleStatement> mainStatements = statements.stream().filter(s -> !AST.FunctionDefinition.class.isInstance(s))
                .collect(Collectors.toList());
        functions.add(new AST.FunctionDefinition("main", Collections.emptyList(), new AST.CompoundStatement(mainStatements)));
        for (final AST.FunctionDefinition f : functions) {
            visitFunctionDefinition(f, compilerContext);
        }
    }

    @Override
    public void visitFunctionDefinition(final AST.FunctionDefinition functionDefinition, final CompilerContext compilerContext) throws Exception {
        compilerContext.wrapFunction(functionDefinition.getName(), scope -> {
            final TypeContext.Scope typeScope = compilerContext.getTypeContext().getScopeByName(scope.getName());
            final List<ConcreteType> types = typeScope.getVariableTypes();
            final ConcreteType returnType = typeScope.getReturnType();
            compilerContext.registerFunction(functionDefinition.getName(), functionDefinition.getVariables().size(), types, returnType);

            for (int i = 0; i < functionDefinition.getVariables().size(); ++i) {
                final AST.VariableDefinition variableDefinition = functionDefinition.getVariables().get(i);
                visitVariableDefinition(variableDefinition, compilerContext);
            }
            visitCompoundStatement(functionDefinition.getCompoundStatement(), compilerContext);
        });
    }

    @Override
    public void visitVariableDefinition(final AST.VariableDefinition variableDefinition, final CompilerContext compilerContext) {
        final String name = variableDefinition.getName();
        final int id = compilerContext.getScope().rename(name);
        log.info("Function {}: rename variable {} to {}", compilerContext.getScope().getName(), name, id);
    }

    @Override
    public void visitAssignmentStatement(final AST.AssignmentStatement assignmentStatement, final CompilerContext compilerContext) throws Exception {
        if (assignmentStatement.getMemoryAccess() instanceof AST.VariableAccessExpression) {
            final AST.VariableAccessExpression variableAccessExpression = (AST.VariableAccessExpression) assignmentStatement.getMemoryAccess();
            final String name = variableAccessExpression.getName();
            final int id = compilerContext.getScope().rename(name);
            // expression
            visitExpression(assignmentStatement.getExpression(), compilerContext);
            //
            final ConcreteType type = compilerContext.getTypeContext().getScopeByName(compilerContext.getScope().getName()).getVariableType(id);
            if (type.equals(IntegerType.INSTANCE)) {
                compilerContext.addCommand(new VM.IStore(id));
            } else if (type.equals(CharacterType.INSTANCE)) {
                compilerContext.addCommand(new VM.IStore(id));
            } else {
                compilerContext.addCommand(new VM.AStore(id));
            }
        } else {
            final AST.ArrayAccessExpression arrayAccessExpression = (AST.ArrayAccessExpression) assignmentStatement.getMemoryAccess();
            visitMemoryAccess(arrayAccessExpression.getPointer(), compilerContext);
            visitExpression(arrayAccessExpression.getExpression(), compilerContext);
            // expression
            visitExpression(assignmentStatement.getExpression(), compilerContext);
            //
            final Type elementType = getArrayType(arrayAccessExpression.getPointer(), compilerContext).getRight();
            if (elementType.equals(IntegerType.INSTANCE)) {
                compilerContext.addCommand(new VM.IAStore());
            } else if (elementType.equals(CharacterType.INSTANCE)) {
                compilerContext.addCommand(new VM.IAStore());
            } else {
                compilerContext.addCommand(new VM.AAStore());
            }
        }
    }

    private ImplicationType getArrayType(final AST.MemoryAccessExpression memoryAccess, final CompilerContext compilerContext) {
        if (memoryAccess instanceof AST.VariableAccessExpression) {
            final String name = ((AST.VariableAccessExpression) memoryAccess).getName();
            final int id = compilerContext.getScope().rename(name);
            return (ImplicationType) compilerContext.getTypeContext().getScopeByName(compilerContext.getScope().getName()).getVariableType(id);
        }
        if (memoryAccess instanceof AST.ArrayAccessExpression) {
            ConcreteType concreteType = getArrayType(((AST.ArrayAccessExpression) memoryAccess).getPointer(), compilerContext);
            assert ImplicationType.class.isInstance(concreteType);
            return (ImplicationType) ((ImplicationType) concreteType).getRight();
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitIfStatement(final AST.IfStatement ifStatement, final CompilerContext context) throws Exception {
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
    public void visitGotoStatement(final AST.GotoStatement gotoStatement, final CompilerContext compilerContext) {
        compilerContext.addCommand(new VM.Goto(gotoStatement.getLabel()));
    }

    @Override
    public void visitContinueStatement(final AST.ContinueStatement continueStatement, final CompilerContext compilerContext) {
        compilerContext.addCommand(new VM.Goto(compilerContext.getLoop().getLoopLabel()));
    }

    @Override
    public void visitBreakStatement(final AST.BreakStatement breakStatement, final CompilerContext compilerContext) {
        compilerContext.addCommand(new VM.Goto(compilerContext.getLoop().getEndLabel()));
    }

    @Override
    public void visitReturnStatement(final AST.ReturnStatement returnStatement, final CompilerContext compilerContext) throws Exception {
        if (returnStatement.getExpression() != null) {
            visitExpression(returnStatement.getExpression(), compilerContext);
        }
        final ConcreteType returnType = compilerContext.getTypeContext().getScopeByName(compilerContext.getScope().getName()).getReturnType();
        if (returnType.equals(VoidType.INSTANCE)) {
            compilerContext.addCommand(new VM.Return());
        } else if (returnType.equals(IntegerType.INSTANCE)) {
            compilerContext.addCommand(new VM.IReturn());
        } else if (returnType.equals(CharacterType.INSTANCE)) {
            compilerContext.addCommand(new VM.IReturn());
        } else {
            compilerContext.addCommand(new VM.AReturn());
        }
    }

    @Override
    public void visitSkipStatement(final AST.SkipStatement skipStatement, final CompilerContext compilerContext) throws Exception {
        // do nothing
    }

    @Override
    public void visitWhileStatement(final AST.WhileStatement whileStatement, final CompilerContext compilerContext) throws Exception {
        final String loopLabel = compilerContext.getNextLabel();
        final String endLabel = compilerContext.getNextLabel();
        visitExpression(whileStatement.getCondition(), compilerContext);
        compilerContext.addCommand(new VM.IfFalse(endLabel));
        compilerContext.addCommand(new VM.Label(loopLabel));
        compilerContext.wrapLoop(loopLabel, endLabel, loop -> {
            visitCompoundStatement(whileStatement.getCompoundStatement(), compilerContext);
            visitExpression(whileStatement.getCondition(), compilerContext);
            compilerContext.addCommand(new VM.IfTrue(loopLabel));
        });
        compilerContext.addCommand(new VM.Label(endLabel));
    }

    @Override
    public void visitRepeatStatement(final AST.RepeatStatement repeatStatement, final CompilerContext compilerContext) throws Exception {
        final String loopLabel = compilerContext.getNextLabel();
        final String endLabel = compilerContext.getNextLabel();
        compilerContext.addCommand(new VM.Label(loopLabel));
        compilerContext.wrapLoop(loopLabel, endLabel, loop -> {
            visitCompoundStatement(repeatStatement.getCompoundStatement(), compilerContext);
            visitExpression(repeatStatement.getCondition(), compilerContext);
            compilerContext.addCommand(new VM.IfFalse(loopLabel));
        });
        compilerContext.addCommand(new VM.Label(endLabel));
    }

    @Override
    public void visitForStatement(final AST.ForStatement forStatement, final CompilerContext compilerContext) throws Exception {
        compilerContext.addCommand(new VM.Comment("Start: for"));
        if (forStatement.getInit() != null) {
            visitAssignmentStatement(forStatement.getInit(), compilerContext);
        }
        final String loopLabel = compilerContext.getNextLabel();
        final String endLabel = compilerContext.getNextLabel();
        final String continueLabel = compilerContext.getNextLabel();
        compilerContext.addCommand(new VM.Label(loopLabel));
        compilerContext.wrapLoop(continueLabel, endLabel, loop -> {
            if (forStatement.getCondition() != null) {
                visitExpression(forStatement.getCondition(), compilerContext);
                compilerContext.addCommand(new VM.IfFalse(endLabel));
            }
            visitCompoundStatement(forStatement.getCompoundStatement(), compilerContext);
            compilerContext.addCommand(new VM.Label(continueLabel));
            if (forStatement.getLoop() != null) {
                visitAssignmentStatement(forStatement.getLoop(), compilerContext);
            }
            compilerContext.addCommand(new VM.Goto(loopLabel));
        });
        compilerContext.addCommand(new VM.Label(endLabel));
        compilerContext.addCommand(new VM.Comment("End: for"));
    }

    @Override
    public void visitFunctionCall(final AST.FunctionCall functionCall, final CompilerContext compilerContext) throws Exception {
        final List<AST.Expression> arguments = functionCall.getArguments();
        for (int i = 0; i < arguments.size(); ++i) {
            final AST.Expression expression = arguments.get(i);
            visitExpression(expression, compilerContext);
        }
        compilerContext.addCommand(new VM.InvokeStatic(functionCall.getName(), functionCall.getArguments().size()));
    }

    @Override
    public void visitArrayCreation(final AST.ArrayCreationExpression arrayCreationExpression, final CompilerContext compilerContext) throws Exception {
        final List<AST.Expression> arguments = arrayCreationExpression.getArguments();
        final int length = arguments.size();
        compilerContext.addCommand(new VM.IConst(length));
        compilerContext.addCommand(new VM.NewArray());
        for (int i = 0; i < length; ++i) {
            compilerContext.addCommand(new VM.Dup());
            compilerContext.addCommand(new VM.IConst(i));
            visitExpression(arguments.get(i), compilerContext);
            compilerContext.addCommand(new VM.IAStore());
        }
    }

    @Override
    public void visitVariableAccess(final AST.VariableAccessExpression variableAccessExpression, final CompilerContext compilerContext) throws Exception {
        final String name = variableAccessExpression.getName();
        if ("true".equals(name)) {
            compilerContext.addCommand(new VM.IConst(1));
        } else if ("false".equals(name)) {
            compilerContext.addCommand(new VM.IConst(0));
        } else {
            final int id = compilerContext.getScope().rename(name);
            final ConcreteType type = compilerContext.getTypeContext().getScopeByName(compilerContext.getScope().getName()).getVariableType(id);
            if (type.equals(IntegerType.INSTANCE)) {
                compilerContext.addCommand(new VM.ILoad(id));
            } else if (type.equals(CharacterType.INSTANCE)) {
                compilerContext.addCommand(new VM.ILoad(id));
            } else {
                compilerContext.addCommand(new VM.ALoad(id));
            }
        }
    }

    @Override
    public void visitArrayAccess(final AST.ArrayAccessExpression arrayAccessExpression, final CompilerContext compilerContext) throws Exception {
        visitMemoryAccess(arrayAccessExpression.getPointer(), compilerContext);
        visitExpression(arrayAccessExpression.getExpression(), compilerContext);
        final Type elementType = getArrayType(arrayAccessExpression.getPointer(), compilerContext).getRight();
        if (elementType.equals(IntegerType.INSTANCE)) {
            compilerContext.addCommand(new VM.IALoad());
        } else if (elementType.equals(CharacterType.INSTANCE)) {
            compilerContext.addCommand(new VM.IALoad());
        } else {
            compilerContext.addCommand(new VM.AALoad());
        }
    }

    @Override
    public void visitVariableAccessForWrite(final AST.VariableAccessExpression variableAccessExpression, final CompilerContext compilerContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitArrayAccessForWrite(final AST.ArrayAccessExpression arrayAccessExpression, final CompilerContext compilerContext) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitIntegerLiteral(final AST.IntegerLiteral integerLiteral, final CompilerContext compilerContext) throws Exception {
        compilerContext.addCommand(new VM.IConst(integerLiteral.getValue()));
    }

    @Override
    public void visitCharacterLiteral(final AST.CharacterLiteral characterLiteral, final CompilerContext compilerContext) throws Exception {
        final char character = characterLiteral.getValue();
        compilerContext.addCommand(new VM.IConst(character));
    }

    @Override
    public void visitStringLiteral(final AST.StringLiteral stringLiteral, final CompilerContext compilerContext) throws Exception {
        final String string = stringLiteral.getValue();
        final int length = string.length();
        compilerContext.addCommand(new VM.IConst(length));
        compilerContext.addCommand(new VM.NewArray());
        for (int i = 0; i < length; ++i) {
            compilerContext.addCommand(new VM.Dup());
            compilerContext.addCommand(new VM.IConst(i));
            compilerContext.addCommand(new VM.IConst(string.charAt(i)));
            compilerContext.addCommand(new VM.IAStore());
        }
    }

    @Override
    public void visitNullLiteral(final AST.NullLiteral nullLiteral, final CompilerContext compilerContext) throws Exception {
        compilerContext.addCommand(new VM.AConstNull());
    }

    @Override
    public void visitUnaryExpression(final AST.UnaryExpression unaryExpression, final CompilerContext compilerContext) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitBinaryExpression(final AST.BinaryExpression binaryExpression, final CompilerContext compilerContext) throws Exception {
        visitExpression(binaryExpression.getLeft(), compilerContext);
        visitExpression(binaryExpression.getRight(), compilerContext);
        compilerContext.addCommand(new VM.BinOp(binaryExpression.getOperator()));
    }
}
