package ru.ifmo.ctddev.khovanskiy.compilers.vm.visitor;

import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMFunction;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;

import java.util.List;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public abstract class AbstractVMVisitor<C> implements VMVisitor<C> {
    @Override
    public void visitProgram(VMProgram vmProgram, C c) throws Exception {
        for (VMFunction function : vmProgram.getFunctions()) {
            visitFunction(function, c);
        }
    }

    @Override
    public void visitFunction(VMFunction function, C c) throws Exception {
        List<VM> commands = function.getCommands();
        for (int i = 0; i < commands.size(); ++i) {
            VM command = commands.get(i);
            visitCommand(command, c);
        }
    }

    @Override
    public void visitCommand(VM vm, C c) throws Exception {
        if (vm instanceof VM.Comment) {
            visitComment((VM.Comment) vm, c);
            return;
        }
        if (vm instanceof VM.Dup) {
            visitDup((VM.Dup) vm, c);
            return;
        }
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
        if (vm instanceof VM.NewArray) {
            visitNewArray((VM.NewArray) vm, c);
            return;
        }
        visitUnknown(vm, c);
    }

    @Override
    public void visitComment(VM.Comment comment, C c) throws Exception {
    }

    @Override
    public void visitStore(VM.Store store, C c) throws Exception {
        if (store instanceof VM.IStore) {
            visitIStore((VM.IStore) store, c);
            return;
        }
        if (store instanceof VM.AStore) {
            visitAStore((VM.AStore) store, c);
            return;
        }
        if (store instanceof VM.IAStore) {
            visitIAStore((VM.IAStore) store, c);
            return;
        }
        if (store instanceof VM.AAStore) {
            visitAAStore((VM.AAStore) store, c);
            return;
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitLoad(VM.Load load, C c) throws Exception {
        if (load instanceof VM.ILoad) {
            visitILoad((VM.ILoad) load, c);
            return;
        }
        if (load instanceof VM.ALoad) {
            visitALoad((VM.ALoad) load, c);
            return;
        }
        if (load instanceof VM.IALoad) {
            visitIALoad((VM.IALoad) load, c);
            return;
        }
        if (load instanceof VM.AALoad) {
            visitAALoad((VM.AALoad) load, c);
            return;
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitConst(VM.Const command, C c) throws Exception {
        if (command instanceof VM.IConst) {
            visitIConst((VM.IConst) command, c);
            return;
        }
        if (command instanceof VM.AConst) {
            visitAConst((VM.AConst) command, c);
            return;
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitAConst(VM.AConst command, C c) throws Exception {
        if (command instanceof VM.AConstNull) {
            visitAConstNull((VM.AConstNull) command, c);
            return;
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitAbstractInvoke(VM.AbstractInvoke command, C c) throws Exception {
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
        if (command instanceof VM.AReturn) {
            visitAReturn((VM.AReturn) command, c);
            return;
        }
        throw new IllegalStateException();
    }
}
