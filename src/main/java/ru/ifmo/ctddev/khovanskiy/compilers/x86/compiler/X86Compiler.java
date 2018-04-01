package ru.ifmo.ctddev.khovanskiy.compilers.x86.compiler;

import lombok.extern.slf4j.Slf4j;
import ru.ifmo.ctddev.khovanskiy.compilers.Compiler;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMFunction;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.visitor.AbstractVMVisitor;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.Immediate;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.MemoryAccess;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.X86;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.X86Program;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Eax;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Ebp;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Edx;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Esp;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Slf4j
public class X86Compiler extends AbstractVMVisitor<CompilerContext> implements Compiler<VMProgram, X86Program> {
    @Override
    public X86Program compile(VMProgram vmProgram) throws Exception {
        final CompilerContext compilerContext = new CompilerContext();
        visitProgram(vmProgram, compilerContext);
        verify(compilerContext);
        return new X86Program(compilerContext.getCommands());
    }

    protected void verify(CompilerContext compilerContext) {
        int count = 0;
        for (X86 command : compilerContext.getCommands()) {
            if (command instanceof X86.PushL) {
                ++count;
            } else if (command instanceof X86.PopL) {
                --count;
            }
        }
        assert count == 0;
    }

    @Override
    public void visitFunction(final VMFunction function, final CompilerContext compilerContext) throws Exception {
        int maxVariableId = -1;
        for (VM command : function.getCommands()) {
            if (command instanceof VM.IStore) {
                final VM.IStore store = (VM.IStore) command;
                maxVariableId = Math.max(maxVariableId, store.getName());
            } else if (command instanceof VM.ILoad) {
                final VM.ILoad load = (VM.ILoad) command;
                maxVariableId = Math.max(maxVariableId, load.getName());
            }
        }
        int argumentsCount = function.getArgumentsCount();
        int localVariablesCount = maxVariableId - argumentsCount + 1;
        log.info("Function \"{}\" has {} arguments and {} local variables", function.getName(), argumentsCount, localVariablesCount);
        compilerContext.addCommand(new X86.Label(function.getName()));
        compilerContext.addCommand(new X86.PushL(Ebp.INSTANCE));
        compilerContext.addCommand(new X86.MovL(Esp.INSTANCE, Ebp.INSTANCE));

        compilerContext.enterScope();
        for (int i = 0; i < argumentsCount; ++i) {
            compilerContext.registerArgument(i);
        }
        for (int i = 0; i < localVariablesCount; ++i) {
            compilerContext.registerVariable(i);
        }
        super.visitFunction(function, compilerContext);
        final CompilerContext.Scope scope = compilerContext.leaveScope();
        compilerContext.addCommand(new X86.SubL(new Immediate(scope.getMaxAllocated()), Esp.INSTANCE));
        for (final X86 command : scope.getCommands()) {
            compilerContext.addCommand(command);
        }

        compilerContext.addCommand(new X86.MovL(Ebp.INSTANCE, Esp.INSTANCE));
        compilerContext.addCommand(new X86.PopL(Ebp.INSTANCE));
        compilerContext.addCommand(new X86.XorL(Eax.INSTANCE, Eax.INSTANCE));
        compilerContext.addCommand(new X86.Ret());
    }

    @Override
    public void visitDup(VM.Dup dup, CompilerContext compilerContext) throws Exception {

    }

    @Override
    public void visitIStore(VM.IStore iStore, CompilerContext compilerContext) throws Exception {
        final MemoryAccess temporary = compilerContext.pop();
        final MemoryAccess variable = compilerContext.get(iStore.getName());
        compilerContext.getScope().move(temporary, variable);
    }

    @Override
    public void visitIAStore(VM.IAStore iaStore, CompilerContext compilerContext) throws Exception {

    }

    @Override
    public void visitILoad(VM.ILoad iLoad, CompilerContext compilerContext) throws Exception {
        final MemoryAccess variable = compilerContext.get(iLoad.getName());
        final MemoryAccess temporary = compilerContext.allocate();
        compilerContext.getScope().move(variable, temporary);
    }

    @Override
    public void visitIALoad(VM.IALoad iaLoad, CompilerContext compilerContext) throws Exception {

    }

    @Override
    public void visitLabel(VM.Label label, CompilerContext compilerContext) throws Exception {
        compilerContext.getScope().addCommand(new X86.Label(label.getName()));
    }

    @Override
    public void visitBinOp(VM.BinOp binOp, CompilerContext compilerContext) throws Exception {
        switch (binOp.getOperator()) {
            case "+":
                visitIAdd(compilerContext);
                break;
            case "-":
                visitISub(compilerContext);
                break;
            case "*":
                visitIMul(compilerContext);
                break;
            case "/":
                visitIDiv(compilerContext);
                break;
            case "%":
                visitIRem(compilerContext);
                return;
            default:
                throw new UnsupportedOperationException(binOp.getOperator());
        }
    }

    public void visitISub(CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.SubL(rhs, Eax.INSTANCE));
        final MemoryAccess result = compilerContext.allocate();
        compilerContext.getScope().move(Eax.INSTANCE, result);
    }

    public void visitIAdd(CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.AddL(rhs, Eax.INSTANCE));
        final MemoryAccess result = compilerContext.allocate();
        compilerContext.getScope().move(Eax.INSTANCE, result);
    }

    public void visitIMul(CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.ImulL(rhs, Eax.INSTANCE));
        final MemoryAccess result = compilerContext.allocate();
        compilerContext.getScope().move(Eax.INSTANCE, result);
    }

    public void visitIDiv(CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.Cltd());
        compilerContext.getScope().addCommand(new X86.IDivL(rhs));
        final MemoryAccess result = compilerContext.allocate();
        compilerContext.getScope().move(Eax.INSTANCE, result);
    }

    public void visitIRem(CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.Cltd());
        compilerContext.getScope().addCommand(new X86.IDivL(rhs));
        final MemoryAccess result = compilerContext.allocate();
        compilerContext.getScope().move(Edx.INSTANCE, result);
    }

    @Override
    public void visitAConstNull(VM.AConstNull aConstNull, CompilerContext compilerContext) throws Exception {

    }

    @Override
    public void visitIConst(VM.IConst iConst, CompilerContext compilerContext) throws Exception {
        final MemoryAccess temporary = compilerContext.allocate();
        compilerContext.getScope().move(new Immediate(iConst.getValue()), temporary);
    }

    @Override
    public void visitInvokeStatic(VM.InvokeStatic call, CompilerContext compilerContext) throws Exception {
        assert call.getArgumentsCount() == compilerContext.getStack().size();
        for (int i = 0; i < call.getArgumentsCount(); ++i) {
            MemoryAccess memoryAccess = compilerContext.pop();
            compilerContext.getScope().addCommand(new X86.PushL(memoryAccess));
        }
        compilerContext.getScope().addCommand(new X86.Call(call.getName()));
        if (!call.getName().equals("write")) { // todo: if returns void type
            final MemoryAccess temporary = compilerContext.allocate();
            compilerContext.getScope().move(Eax.INSTANCE, temporary);
        }
        for (int i = 0; i < call.getArgumentsCount(); ++i) {
            compilerContext.getScope().addCommand(new X86.PopL(Eax.INSTANCE));
        }
    }

    @Override
    public void visitReturn(VM.Return vmReturn, CompilerContext compilerContext) throws Exception {

    }

    @Override
    public void visitIReturn(VM.IReturn iReturn, CompilerContext compilerContext) throws Exception {

    }

    @Override
    public void visitGoto(VM.Goto vmGoto, CompilerContext compilerContext) throws Exception {

    }

    @Override
    public void visitIfTrue(VM.IfTrue ifTrue, CompilerContext compilerContext) throws Exception {

    }

    @Override
    public void visitIfFalse(VM.IfFalse ifFalse, CompilerContext compilerContext) throws Exception {

    }

    @Override
    public void visitNewArray(VM.NewArray newArray, CompilerContext compilerContext) throws Exception {

    }

    @Override
    public void visitUnknown(VM vm, CompilerContext compilerContext) throws Exception {
        throw new UnsupportedOperationException();
    }
}
