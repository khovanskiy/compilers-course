package ru.ifmo.ctddev.khovanskiy.compilers.vm.printer;

import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMFunction;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.visitor.AbstractVMVisitor;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class VMPrinter extends AbstractVMVisitor<PrinterContext> {
    @Override
    public void visitProgram(VMProgram vmProgram, PrinterContext context) {
        for (VMFunction function : vmProgram.getFunctions()) {
            visitFunction(function, context);
        }
        context.flush();
    }

    @Override
    public void visitFunction(VMFunction function, PrinterContext context) {
        context.append("begin " + function.getName() + "\n");
        context.setLineNumber(0);
        for (VM command : function.getCommands()) {
            visitCommand(command, context);
        }
        context.append("end\n\n");
    }

    @Override
    public void visitComment(VM.Comment comment, PrinterContext context) {
        context.printLine("// " + comment.getText());
    }

    @Override
    public void visitDup(VM.Dup command, PrinterContext context) {
        context.printLine("dup");
    }

    @Override
    public void visitIStore(VM.IStore command, PrinterContext context) {
        context.printLine("i_store v" + command.getName());
    }

    @Override
    public void visitAStore(VM.AStore command, PrinterContext context) {
        context.printLine("a_store v" + command.getName());
    }

    @Override
    public void visitIAStore(VM.IAStore command, PrinterContext context) {
        context.printLine("i_a_store");
    }

    @Override
    public void visitAAStore(VM.AAStore command, PrinterContext context) {
        context.printLine("a_a_store");
    }

    @Override
    public void visitILoad(VM.ILoad command, PrinterContext context) {
        context.printLine("i_load v" + command.getName());
    }

    @Override
    public void visitALoad(VM.ALoad command, PrinterContext context) {
        context.printLine("a_load v" + command.getName());
    }

    @Override
    public void visitIALoad(VM.IALoad command, PrinterContext context) {
        context.printLine("i_a_load");
    }

    @Override
    public void visitAALoad(VM.AALoad command, PrinterContext context) {
        context.printLine("a_a_load");
    }

    @Override
    public void visitLabel(VM.Label command, PrinterContext context) {
        context.printLine("label " + command.getName());
    }

    @Override
    public void visitBinOp(VM.BinOp command, PrinterContext context) {
        context.printLine("bin_op " + command.getOperator());
    }

    @Override
    public void visitAConstNull(VM.AConstNull command, PrinterContext context) {
        context.printLine("a_const_null");
    }

    @Override
    public void visitIConst(VM.IConst command, PrinterContext context) {
        context.printLine("i_const " + command.getValue());
    }

    @Override
    public void visitInvokeStatic(VM.InvokeStatic command, PrinterContext context) {
        context.printLine("invoke_static " + command.getName() + "," + command.getArgumentsCount());
    }

    @Override
    public void visitReturn(VM.Return command, PrinterContext context) {
        context.printLine("return");
    }

    @Override
    public void visitIReturn(VM.IReturn command, PrinterContext context) {
        context.printLine("i_return");
    }

    @Override
    public void visitAReturn(VM.AReturn command, PrinterContext context) {
        context.printLine("a_return");
    }

    @Override
    public void visitGoto(VM.Goto command, PrinterContext context) {
        context.printLine("goto " + command.getLabel());
    }

    @Override
    public void visitIfTrue(VM.IfTrue command, PrinterContext context) {
        context.printLine("if_true " + command.getLabel());
    }

    @Override
    public void visitIfFalse(VM.IfFalse command, PrinterContext context) {
        context.printLine("if_false " + command.getLabel());
    }

    @Override
    public void visitNewArray(VM.NewArray command, PrinterContext context) {
        context.printLine("new_array");
    }

    @Override
    public void visitUnknown(VM command, PrinterContext context) {
        context.printLine(command.toString());
    }
}
