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
    public void visitProgram(VMProgram vmProgram, C c) {
        for (final VMFunction function : vmProgram.getFunctions()) {
            visitFunction(function, c);
        }
    }

    @Override
    public void visitFunction(VMFunction function, C c) {
        final List<VM> commands = function.getCommands();
        for (final VM command : commands) {
            visitCommand(command, c);
        }
    }

    @Override
    public void visitCommand(VM vm, C c) {
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
    public void visitComment(VM.Comment comment, C c) {
        // do nothing
    }

    @Override
    public void visitStore(VM.Store command, C c) {
        if (command instanceof VM.IStore) {
            visitIStore((VM.IStore) command, c);
            return;
        }
        if (command instanceof VM.AStore) {
            visitAStore((VM.AStore) command, c);
            return;
        }
        if (command instanceof VM.IAStore) {
            visitIAStore((VM.IAStore) command, c);
            return;
        }
        if (command instanceof VM.AAStore) {
            visitAAStore((VM.AAStore) command, c);
            return;
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitLoad(VM.Load command, C c) {
        if (command instanceof VM.ILoad) {
            visitILoad((VM.ILoad) command, c);
            return;
        }
        if (command instanceof VM.ALoad) {
            visitALoad((VM.ALoad) command, c);
            return;
        }
        if (command instanceof VM.IALoad) {
            visitIALoad((VM.IALoad) command, c);
            return;
        }
        if (command instanceof VM.AALoad) {
            visitAALoad((VM.AALoad) command, c);
            return;
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitConst(VM.Const command, C c) {
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
    public void visitAConst(VM.AConst command, C c) {
        if (command instanceof VM.AConstNull) {
            visitAConstNull((VM.AConstNull) command, c);
            return;
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitAbstractInvoke(VM.AbstractInvoke command, C c) {
        if (command instanceof VM.InvokeStatic) {
            visitInvokeStatic((VM.InvokeStatic) command, c);
            return;
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitAbstractReturn(VM.AbstractReturn command, C c) {
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
