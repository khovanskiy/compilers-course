package ru.ifmo.ctddev.khovanskiy.compilers.vm.printer;

import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.visitor.AbstractVMVisitor;

import java.io.IOException;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class VMPrinter extends AbstractVMVisitor<PrinterContext> {
    @Override
    public void visitProgram(VMProgram vmProgram, PrinterContext context) throws Exception {
        for (VM command : vmProgram.getCommands()) {
            visitCommand(command, context);
        }
        context.flush();
    }

    @Override
    public void visitStore(VM.Store store, PrinterContext context) throws IOException {
        context.printLine("store " + store.getName());
    }

    @Override
    public void visitLoad(VM.Load load, PrinterContext context) throws IOException {
        context.printLine("load " + load.getName());
    }

    @Override
    public void visitLabel(VM.Label label, PrinterContext context) throws IOException {
        context.printLine("label " + label.getName());
    }

    @Override
    public void visitBinOp(VM.BinOp binOp, PrinterContext context) throws IOException {
        context.printLine("BINOP " + binOp.getOperator());
    }

    @Override
    public void visitIConst(VM.IConst iConst, PrinterContext context) throws IOException {
        context.printLine("iconst " + iConst.getValue());
    }

    @Override
    public void visitInvokeExternal(VM.InvokeExternal invokeExternal, PrinterContext context) throws IOException {
        context.printLine("invoke_external " + invokeExternal.getName() + "," + invokeExternal.getArgumentsCount());
    }

    @Override
    public void visitInvokeStatic(VM.InvokeStatic invokeStatic, PrinterContext context) throws IOException {
        context.printLine("invoke_static " + invokeStatic.getName() + "," + invokeStatic.getArgumentsCount());
    }

    @Override
    public void visitReturn(VM.Return vmReturn, PrinterContext context) throws IOException {
        context.printLine("return");
    }

    @Override
    public void visitIReturn(VM.IReturn iReturn, PrinterContext context) throws IOException {
        context.printLine("ireturn");
    }

    @Override
    public void visitGoto(VM.Goto vmGoto, PrinterContext context) throws IOException {
        context.printLine("goto " + vmGoto.getLabel());
    }

    @Override
    public void visitIfTrue(VM.IfTrue ifTrue, PrinterContext context) throws IOException {
        context.printLine("if_true " + ifTrue.getLabel());
    }

    @Override
    public void visitIfFalse(VM.IfFalse ifFalse, PrinterContext context) throws IOException {
        context.printLine("if_false " + ifFalse.getLabel());
    }

    @Override
    public void visitUnknown(VM vm, PrinterContext context) throws IOException {
        context.printLine(vm.toString());
    }
}
