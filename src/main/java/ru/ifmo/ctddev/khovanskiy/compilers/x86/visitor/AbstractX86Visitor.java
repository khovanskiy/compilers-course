package ru.ifmo.ctddev.khovanskiy.compilers.x86.visitor;

import ru.ifmo.ctddev.khovanskiy.compilers.x86.*;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Register;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Register8;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public abstract class AbstractX86Visitor<C> implements X86Visitor<C> {
    @Override
    public void visitProgram(X86Program program, C c) throws Exception {
        for (X86 command : program.getCommands()) {
            visitCommand(command, c);
        }
    }

    @Override
    public void visitCommand(X86 command, C c) throws Exception {
        if (command instanceof X86.PushL) {
            visitPush((X86.PushL) command, c);
            return;
        }
        if (command instanceof X86.PopL) {
            visitPop((X86.PopL) command, c);
            return;
        }
        if (command instanceof X86.MovL) {
            visitMov((X86.MovL) command, c);
            return;
        }
        if (command instanceof X86.Label) {
            visitLabel((X86.Label) command, c);
            return;
        }
        if (command instanceof X86.Call) {
            visitCall((X86.Call) command, c);
            return;
        }
        if (command instanceof X86.Ret) {
            visitRet((X86.Ret) command, c);
            return;
        }
        if (command instanceof X86.AddL) {
            visitAdd((X86.AddL) command, c);
            return;
        }
        if (command instanceof X86.SubL) {
            visitSub((X86.SubL) command, c);
            return;
        }
        if (command instanceof X86.ImulL) {
            visitMul((X86.ImulL) command, c);
            return;
        }
        if (command instanceof X86.IDivL) {
            visitDiv((X86.IDivL) command, c);
            return;
        }
        if (command instanceof X86.Cltd) {
            visitCltd((X86.Cltd) command, c);
            return;
        }
        if (command instanceof X86.Cmp) {
            visitCmp((X86.Cmp) command, c);
            return;
        }
        if (command instanceof X86.Logical) {
            visitLogical((X86.Logical) command, c);
            return;
        }
        if (command instanceof X86.Set) {
            visitSet((X86.Set) command, c);
            return;
        }
        visitUnknown(command, c);
    }

    @Override
    public void visitMemoryAccess(MemoryAccess memoryAccess, C c) throws Exception {
        if (memoryAccess instanceof Register) {
            visitRegister((Register) memoryAccess, c);
            return;
        }
        if (memoryAccess instanceof Register8) {
            visitRegister8((Register8) memoryAccess, c);
            return;
        }
        if (memoryAccess instanceof StackPosition) {
            visitStackPosition((StackPosition) memoryAccess, c);
            return;
        }
        if (memoryAccess instanceof Immediate) {
            visitImmediate((Immediate) memoryAccess, c);
            return;
        }
        throw new IllegalStateException("Unknown memory access instruction");
    }

    @Override
    public void visitLogical(X86.Logical command, C c) throws Exception {
        if (command instanceof X86.AndL) {
            visitAndL((X86.AndL) command, c);
            return;
        }
        if (command instanceof X86.OrL) {
            visitOrL((X86.OrL) command, c);
            return;
        }
        if (command instanceof X86.XorL) {
            visitXorL((X86.XorL) command, c);
            return;
        }
        throw new IllegalStateException("Unknown logical instruction");
    }

    @Override
    public void visitSet(X86.Set command, C c) throws Exception {
        if (command instanceof X86.SetG) {
            visitSetG((X86.SetG) command, c);
            return;
        }
        if (command instanceof X86.SetGe) {
            visitSetGe((X86.SetGe) command, c);
            return;
        }
        if (command instanceof X86.SetL) {
            visitSetL((X86.SetL) command, c);
            return;
        }
        if (command instanceof X86.SetLe) {
            visitSetLe((X86.SetLe) command, c);
            return;
        }
        if (command instanceof X86.SetE) {
            visitSetE((X86.SetE) command, c);
            return;
        }
        if (command instanceof X86.SetNe) {
            visitSetNe((X86.SetNe) command, c);
            return;
        }
        if (command instanceof X86.SetNz) {
            visitSetNz((X86.SetNz) command, c);
            return;
        }
        throw new IllegalStateException("Unknown set instruction");
    }
}
