package ru.ifmo.ctddev.khovanskiy.compilers.x86.visitor;

import ru.ifmo.ctddev.khovanskiy.compilers.x86.*;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Register;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Register8;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public interface X86Visitor<C> {
    void visitProgram(X86Program program, C c) throws Exception;

    void visitCommand(X86 command, C c) throws Exception;

    void visitPush(X86.PushL push, C c) throws Exception;

    void visitPop(X86.PopL pop, C c) throws Exception;

    void visitMov(X86.MovL mov, C c) throws Exception;

    void visitMemoryAccess(MemoryAccess memoryAccess, C c) throws Exception;

    void visitRegister(Register register, C c) throws Exception;

    void visitRegister8(Register8 register, C c) throws Exception;

    void visitStackPosition(StackPosition stackPosition, C c) throws Exception;

    void visitImmediate(Immediate immediate, C context) throws Exception;

    void visitLabel(X86.Label label, C c) throws Exception;

    void visitCall(X86.Call call, C c) throws Exception;

    void visitRet(X86.Ret ret, C c) throws Exception;

    void visitAdd(X86.AddL addL, C c) throws Exception;

    void visitSub(X86.SubL subL, C c) throws Exception;

    void visitMul(X86.ImulL mul, C c) throws Exception;

    void visitDiv(X86.IDivL div, C c) throws Exception;

    void visitCltd(X86.Cltd cltd, C c) throws Exception;

    void visitCmp(X86.Cmp cmp, C c) throws Exception;

    void visitLogical(X86.Logical logical, C c) throws Exception;

    void visitAndL(X86.AndL andL, C c) throws Exception;

    void visitOrL(X86.OrL orL, C c) throws Exception;

    void visitXorL(X86.XorL xorL, C c) throws Exception;

    void visitSet(X86.Set set, C c) throws Exception;

    void visitSetG(X86.SetG setG, C c) throws Exception;

    void visitSetGe(X86.SetGe setGe, C c) throws Exception;

    void visitSetL(X86.SetL setL, C c) throws Exception;

    void visitSetLe(X86.SetLe setLe, C c) throws Exception;

    void visitSetE(X86.SetE setE, C c) throws Exception;

    void visitSetNe(X86.SetNe setNe, C c) throws Exception;

    void visitSetNz(X86.SetNz setNz, C c) throws Exception;

    void visitUnknown(X86 command, C c) throws Exception;
}
