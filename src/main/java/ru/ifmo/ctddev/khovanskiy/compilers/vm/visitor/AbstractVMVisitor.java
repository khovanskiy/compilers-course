package ru.ifmo.ctddev.khovanskiy.compilers.vm.visitor;

import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public abstract class AbstractVMVisitor<C> implements VMVisitor<C> {
    @Override
    public void visitProgram(VMProgram vmProgram, C c) throws Exception {
        for (VM command : vmProgram.getCommands()) {
            visitCommand(command, c);
        }
    }

    @Override
    public void visitCommand(VM vm, C c) throws Exception {
        if (vm instanceof VM.Store) {
            visitStore((VM.Store) vm, c);
            return;
        }
        if (vm instanceof VM.Load) {
            visitLoad((VM.Load) vm, c);
            return;
        }
        if (vm instanceof VM.Label) {
            visitLabel((VM.Label) vm, c);
            return;
        }
        if (vm instanceof VM.Goto) {
            visitGoto((VM.Goto) vm, c);
            return;
        }
        if (vm instanceof VM.IfTrue) {
            visitIfTrue((VM.IfTrue) vm, c);
            return;
        }
        if (vm instanceof VM.IfFalse) {
            visitIfFalse((VM.IfFalse) vm, c);
            return;
        }
        if (vm instanceof VM.AbstractInvoke) {
            visitAbstractInvoke((VM.AbstractInvoke) vm, c);
            return;
        }
        if (vm instanceof VM.BinOp) {
            visitBinOp((VM.BinOp) vm, c);
            return;
        }
        if (vm instanceof VM.Const) {
            visitConst((VM.Const) vm, c);
            return;
        }
        if (vm instanceof VM.AbstractReturn) {
            visitAbstractReturn((VM.AbstractReturn) vm, c);
            return;
        }
        visitUnknown(vm, c);
    }

    @Override
    public void visitConst(VM.Const command, C c) throws Exception {
        if (command instanceof VM.IConst) {
            visitIConst((VM.IConst) command, c);
            return;
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitAbstractInvoke(VM.AbstractInvoke command, C c) throws Exception {
        if (command instanceof VM.InvokeExternal) {
            visitInvokeExternal((VM.InvokeExternal) command, c);
            return;
        }
        if (command instanceof VM.InvokeStatic) {
            visitInvokeStatic((VM.InvokeStatic) command, c);
            return;
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitAbstractReturn(VM.AbstractReturn command, C c) throws Exception {
        if (command instanceof VM.Return) {
            visitReturn((VM.Return) command, c);
            return;
        }
        if (command instanceof VM.IReturn) {
            visitIReturn((VM.IReturn) command, c);
            return;
        }
        throw new IllegalStateException();
    }
}
