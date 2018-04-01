package ru.ifmo.ctddev.khovanskiy.compilers.x86.visitor;

import ru.ifmo.ctddev.khovanskiy.compilers.x86.*;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Register;

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
        if (command instanceof X86.XorL) {
            visitXorL((X86.XorL) command, c);
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
        if (memoryAccess instanceof StackPosition) {
            visitStackPosition((StackPosition) memoryAccess, c);
            return;
        }
        if (memoryAccess instanceof Immediate) {
            visitImmediate((Immediate) memoryAccess, c);
            return;
        }
        throw new UnsupportedOperationException();
    }
}
