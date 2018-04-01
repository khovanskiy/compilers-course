package ru.ifmo.ctddev.khovanskiy.compilers.vm.printer;

import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMFunction;
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
        for (VMFunction function : vmProgram.getFunctions()) {
            visitFunction(function, context);
        }
        context.flush();
    }

    @Override
    public void visitFunction(VMFunction function, PrinterContext context) throws Exception {
        context.append("begin " + function.getName() + "\n");
        context.setLineNumber(0);
        for (VM command : function.getCommands()) {
            visitCommand(command, context);
        }
        context.append("end\n\n");
    }

    @Override
    public void visitComment(VM.Comment comment, PrinterContext context) throws Exception {
        context.printLine("// " + comment.getText());
    }

    @Override
    public void visitDup(VM.Dup dup, PrinterContext context) throws IOException {
        context.printLine("dup");
    }

    @Override
    public void visitIStore(VM.IStore store, PrinterContext context) throws IOException {
        context.printLine("i_store v" + store.getName());
    }

    @Override
    public void visitIAStore(VM.IAStore iaStore, PrinterContext context) throws IOException {
        context.printLine("i_a_store");
    }

    @Override
    public void visitILoad(VM.ILoad load, PrinterContext context) throws IOException {
        context.printLine("load v" + load.getName());
    }

    @Override
    public void visitIALoad(VM.IALoad iaLoad, PrinterContext context) throws IOException {
        context.printLine("i_a_load");
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
    public void visitAConstNull(VM.AConstNull aConstNull, PrinterContext context) throws IOException {
        context.printLine("a_const_null");
    }

    @Override
    public void visitIConst(VM.IConst iConst, PrinterContext context) throws IOException {
        context.printLine("i_const " + iConst.getValue());
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
        context.printLine("i_return");
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
    public void visitNewArray(VM.NewArray newArray, PrinterContext context) throws IOException {
        context.printLine("new_array");
    }

    @Override
    public void visitUnknown(VM vm, PrinterContext context) throws IOException {
        context.printLine(vm.toString());
    }
}
