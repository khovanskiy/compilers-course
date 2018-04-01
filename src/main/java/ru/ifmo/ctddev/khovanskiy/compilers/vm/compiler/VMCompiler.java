package ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler;

import lombok.extern.slf4j.Slf4j;
import ru.ifmo.ctddev.khovanskiy.compilers.Compiler;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.visitor.AbstractASTVisitor;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class VMCompiler extends AbstractASTVisitor<CompilerContext> implements Compiler<AST.CompilationUnit, CompilerContext> {
    @Override
    public CompilerContext compile(AST.CompilationUnit ast) throws Exception {
        final CompilerContext program = new CompilerContext();
        visitCompilationUnit(ast, program);
        return program;
    }

    @Override
    public void visitCompilationUnit(AST.CompilationUnit compilationUnit, CompilerContext compilerContext) throws Exception {
        List<AST.SingleStatement> statements = compilationUnit.getCompoundStatement().getStatements();
        List<AST.FunctionDefinition> functions = statements.stream()
                .filter(AST.FunctionDefinition.class::isInstance)
                .map(AST.FunctionDefinition.class::cast)
                .collect(Collectors.toList());
        List<AST.SingleStatement> mainStatements = statements.stream().filter(s -> !AST.FunctionDefinition.class.isInstance(s))
                .collect(Collectors.toList());
        functions.add(new AST.FunctionDefinition("main", Collections.emptyList(), new AST.CompoundStatement(mainStatements)));
        for (AST.FunctionDefinition f : functions) {
            visitFunctionDefinition(f, compilerContext);
        }
    }

    @Override
    public void visitFunctionDefinition(AST.FunctionDefinition functionDefinition, CompilerContext compilerContext) throws Exception {
        compilerContext.registerFunction(functionDefinition.getName(), functionDefinition.getVariables().size());

//        compilerContext.addCommand(new VM.Label(functionDefinition.getName()));
        compilerContext.getScopes().push(new CompilerContext.Scope());
        for (int i = 0; i < functionDefinition.getVariables().size(); ++i) {
            AST.VariableDefinition variableDefinition = functionDefinition.getVariables().get(i);
            String name = variableDefinition.getName();
            final int id = compilerContext.getScope().rename(name);
            log.info("Function {}: rename variable {} to {}", functionDefinition.getName(), name, id);
        }
        visitCompoundStatement(functionDefinition.getCompoundStatement(), compilerContext);
        compilerContext.getScopes().pop();

    }

    @Override
    public void visitVariableDefinition(AST.VariableDefinition variableDefinition, CompilerContext compilerContext) throws Exception {

    }

    @Override
    public void visitAssignmentStatement(AST.AssignmentStatement assignmentStatement, CompilerContext compilerContext) throws Exception {
        if (assignmentStatement.getMemoryAccess() instanceof AST.VariableAccessExpression) {
            AST.VariableAccessExpression variableAccessExpression = (AST.VariableAccessExpression) assignmentStatement.getMemoryAccess();
            String oldName = variableAccessExpression.getName();
            final int id = compilerContext.getScope().rename(oldName);
            // expression
            visitExpression(assignmentStatement.getExpression(), compilerContext);
            //
            compilerContext.addCommand(new VM.IStore(id));
        } else {
            AST.ArrayAccessExpression arrayAccessExpression = (AST.ArrayAccessExpression) assignmentStatement.getMemoryAccess();
            visitMemoryAccess(arrayAccessExpression.getPointer(), compilerContext);
            visitExpression(arrayAccessExpression.getExpression(), compilerContext);
            // expression
            visitExpression(assignmentStatement.getExpression(), compilerContext);
            //
            compilerContext.addCommand(new VM.IAStore());
        }
    }

    @Override
    public void visitIfStatement(AST.IfStatement ifStatement, CompilerContext context) throws Exception {
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
    public void visitGotoStatement(AST.GotoStatement gotoStatement, CompilerContext compilerContext) throws Exception {
        compilerContext.addCommand(new VM.Goto(gotoStatement.getLabel()));
    }

    @Override
    public void visitContinueStatement(AST.ContinueStatement continueStatement, CompilerContext compilerContext) throws Exception {

    }

    @Override
    public void visitBreakStatement(AST.BreakStatement breakStatement, CompilerContext compilerContext) throws Exception {

    }

    @Override
    public void visitReturnStatement(AST.ReturnStatement returnStatement, CompilerContext compilerContext) throws Exception {
        visitExpression(returnStatement.getExpression(), compilerContext);
        compilerContext.addCommand(new VM.IReturn());
    }

    @Override
    public void visitSkipStatement(AST.SkipStatement skipStatement, CompilerContext compilerContext) throws Exception {

    }

    @Override
    public void visitWhileStatement(AST.WhileStatement whileStatement, CompilerContext compilerContext) throws Exception {
        final String loopLabel = compilerContext.getNextLabel();
        final String endLabel = compilerContext.getNextLabel();
        visitExpression(whileStatement.getCondition(), compilerContext);
        compilerContext.addCommand(new VM.IfFalse(endLabel));
        compilerContext.addCommand(new VM.Label(loopLabel));
        visitCompoundStatement(whileStatement.getCompoundStatement(), compilerContext);
        visitExpression(whileStatement.getCondition(), compilerContext);
        compilerContext.addCommand(new VM.IfTrue(loopLabel));
        compilerContext.addCommand(new VM.Label(endLabel));
    }

    @Override
    public void visitRepeatStatement(AST.RepeatStatement repeatStatement, CompilerContext compilerContext) throws Exception {
        final String label = compilerContext.getNextLabel();
        compilerContext.addCommand(new VM.Label(label));
        visitCompoundStatement(repeatStatement.getCompoundStatement(), compilerContext);
        visitExpression(repeatStatement.getCondition(), compilerContext);
        compilerContext.addCommand(new VM.IfFalse(label));
    }

    @Override
    public void visitForStatement(AST.ForStatement forStatement, CompilerContext compilerContext) throws Exception {
        compilerContext.addCommand(new VM.Comment("Start: for"));
        if (forStatement.getInit() != null) {
            visitAssignmentStatement(forStatement.getInit(), compilerContext);
        }
        final String loopLabel = compilerContext.getNextLabel();
        final String endLabel = compilerContext.getNextLabel();
        compilerContext.addCommand(new VM.Label(loopLabel));
        if (forStatement.getCondition() != null) {
            visitExpression(forStatement.getCondition(), compilerContext);
            compilerContext.addCommand(new VM.IfFalse(endLabel));
        }
        visitCompoundStatement(forStatement.getCompoundStatement(), compilerContext);
        if (forStatement.getLoop() != null) {
            visitAssignmentStatement(forStatement.getLoop(), compilerContext);
        }
        compilerContext.addCommand(new VM.Goto(loopLabel));
        compilerContext.addCommand(new VM.Label(endLabel));
        compilerContext.addCommand(new VM.Comment("End: for"));
    }

    @Override
    public void visitFunctionCall(AST.FunctionCall functionCall, CompilerContext compilerContext) throws Exception {
        List<AST.Expression> arguments = functionCall.getArguments();
        for (int i = 0; i < arguments.size(); ++i) {
            AST.Expression expression = arguments.get(i);
            visitExpression(expression, compilerContext);
        }
        compilerContext.addCommand(new VM.InvokeStatic(functionCall.getName(), functionCall.getArguments().size()));
    }

    @Override
    public void visitArrayCreation(AST.ArrayCreationExpression arrayCreationExpression, CompilerContext compilerContext) throws Exception {
        List<AST.Expression> arguments = arrayCreationExpression.getArguments();
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
    public void visitVariableAccess(AST.VariableAccessExpression variableAccessExpression, CompilerContext compilerContext) throws Exception {
        String name = variableAccessExpression.getName();
        if ("true".equals(name)) {
            compilerContext.addCommand(new VM.IConst(1));
        } else if ("false".equals(name)) {
            compilerContext.addCommand(new VM.IConst(0));
        } else {
            final int id = compilerContext.getScope().rename(name);
            compilerContext.addCommand(new VM.ILoad(id));
        }
    }

    @Override
    public void visitArrayAccess(AST.ArrayAccessExpression arrayAccessExpression, CompilerContext compilerContext) throws Exception {
        visitMemoryAccess(arrayAccessExpression.getPointer(), compilerContext);
        visitExpression(arrayAccessExpression.getExpression(), compilerContext);
        compilerContext.addCommand(new VM.IALoad());
    }

    @Override
    public void visitVariableAccessForWrite(AST.VariableAccessExpression variableAccessExpression, CompilerContext compilerContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitArrayAccessForWrite(AST.ArrayAccessExpression arrayAccessExpression, CompilerContext compilerContext) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitIntegerLiteral(AST.IntegerLiteral integerLiteral, CompilerContext compilerContext) throws Exception {
        compilerContext.addCommand(new VM.IConst(integerLiteral.getValue()));
    }

    @Override
    public void visitCharacterLiteral(AST.CharacterLiteral characterLiteral, CompilerContext compilerContext) throws Exception {
        final char character = characterLiteral.getValue();
        compilerContext.addCommand(new VM.IConst(character));
    }

    @Override
    public void visitStringLiteral(AST.StringLiteral stringLiteral, CompilerContext compilerContext) throws Exception {
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
    public void visitNullLiteral(AST.NullLiteral nullLiteral, CompilerContext compilerContext) throws Exception {
        compilerContext.addCommand(new VM.AConstNull());
    }

    @Override
    public void visitUnaryExpression(AST.UnaryExpression unaryExpression, CompilerContext compilerContext) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitBinaryExpression(AST.BinaryExpression binaryExpression, CompilerContext compilerContext) throws Exception {
        visitExpression(binaryExpression.getLeft(), compilerContext);
        visitExpression(binaryExpression.getRight(), compilerContext);
        compilerContext.addCommand(new VM.BinOp(binaryExpression.getOperator()));
    }
}
