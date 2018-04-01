package ru.ifmo.ctddev.khovanskiy.compilers.vm.visitor;

import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMFunction;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public interface VMVisitor<C> {
    void visitProgram(VMProgram vmProgram, C c) throws Exception;

    void visitFunction(VMFunction function, C c) throws Exception;

    void visitCommand(VM vm, C c) throws Exception;

    void visitComment(VM.Comment comment, C c) throws Exception;

    void visitDup(VM.Dup dup, C c) throws Exception;

    void visitStore(VM.Store store, C c) throws Exception;

    void visitIStore(VM.IStore iStore, C c) throws Exception;

    void visitIAStore(VM.IAStore iaStore, C c) throws Exception;

    void visitLoad(VM.Load load, C c) throws Exception;

    void visitILoad(VM.ILoad iLoad, C c) throws Exception;

    void visitIALoad(VM.IALoad iaLoad, C c) throws Exception;

    void visitLabel(VM.Label label, C c) throws Exception;

    void visitBinOp(VM.BinOp binOp, C c) throws Exception;

    void visitConst(VM.Const vmConst, C c) throws Exception;

    void visitAConst(VM.AConst aConst, C c) throws Exception;

    void visitAConstNull(VM.AConstNull aConstNull, C c) throws Exception;

    void visitIConst(VM.IConst iConst, C c) throws Exception;

    void visitAbstractInvoke(VM.AbstractInvoke abstractInvoke, C c) throws Exception;

    void visitInvokeStatic(VM.InvokeStatic call, C c) throws Exception;

    void visitAbstractReturn(VM.AbstractReturn abstractReturn, C c) throws Exception;

    void visitReturn(VM.Return vmReturn, C c) throws Exception;

    void visitIReturn(VM.IReturn iReturn, C c) throws Exception;

    void visitGoto(VM.Goto vmGoto, C c) throws Exception;

    void visitIfTrue(VM.IfTrue ifTrue, C c) throws Exception;

    void visitIfFalse(VM.IfFalse ifFalse, C c) throws Exception;

    void visitNewArray(VM.NewArray newArray, C c) throws Exception;

    void visitUnknown(VM vm, C c) throws Exception;
}
