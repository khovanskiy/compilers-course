package ru.ifmo.ctddev.khovanskiy.compilers.x86.printer;

import ru.ifmo.ctddev.khovanskiy.compilers.x86.Immediate;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.StackPosition;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.X86;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.X86Program;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Register;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.visitor.AbstractX86Visitor;

import java.io.IOException;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class X86Printer extends AbstractX86Visitor<X86PrinterContext> {
    @Override
    public void visitProgram(X86Program program, X86PrinterContext context) throws Exception {
        context.printLine("\t.data");
        context.printLine("\t.text");
        context.printLine("\t.globl\tmain");
        super.visitProgram(program, context);
        context.flush();
    }

    @Override
    public void visitPush(X86.PushL push, X86PrinterContext context) throws Exception {
        context.append("\tpushl ");
        visitMemoryAccess(push.getSource(), context);
        context.append("\n");
    }

    @Override
    public void visitPop(X86.PopL pop, X86PrinterContext context) throws Exception {
        context.append("\tpopl ");
        visitMemoryAccess(pop.getDestination(), context);
        context.append("\n");
    }

    @Override
    public void visitMov(X86.MovL mov, X86PrinterContext context) throws Exception {
        context.append("\tmovl ");
        visitMemoryAccess(mov.getSource(), context);
        context.append(", ");
        visitMemoryAccess(mov.getDestination(), context);
        context.append("\n");
    }

    @Override
    public void visitRegister(Register register, X86PrinterContext context) throws IOException {
        context.append("%" + register.getClass().getSimpleName().toLowerCase());
    }

    @Override
    public void visitStackPosition(StackPosition stackPosition, X86PrinterContext context) throws IOException {
        context.append(stackPosition.getPosition() + "(");
        visitRegister(stackPosition.getRegister(), context);
        context.append(")");
    }

    @Override
    public void visitImmediate(Immediate immediate, X86PrinterContext context) throws IOException {
        context.append("$" + immediate.getValue());
    }

    @Override
    public void visitLabel(X86.Label label, X86PrinterContext context) throws Exception {
        context.printLine(label.getName() + ":");
    }

    @Override
    public void visitCall(X86.Call call, X86PrinterContext context) throws Exception {
        context.printLine("\tcall " + call.getLabel());
    }

    @Override
    public void visitRet(X86.Ret ret, X86PrinterContext context) throws IOException {
        context.printLine("\tret");
    }

    @Override
    public void visitAdd(X86.AddL addL, X86PrinterContext context) throws Exception {
        context.append("\taddl ");
        visitMemoryAccess(addL.getSource(), context);
        context.append(", ");
        visitMemoryAccess(addL.getDestination(), context);
        context.append("\n");
    }

    @Override
    public void visitSub(X86.SubL subL, X86PrinterContext context) throws Exception {
        context.append("\tsubl ");
        visitMemoryAccess(subL.getSource(), context);
        context.append(", ");
        visitMemoryAccess(subL.getDestination(), context);
        context.append("\n");
    }

    @Override
    public void visitMul(X86.ImulL mul, X86PrinterContext context) throws Exception {
        context.append("\timull ");
        visitMemoryAccess(mul.getSource(), context);
        context.append(", ");
        visitMemoryAccess(mul.getDestination(), context);
        context.append("\n");
    }

    @Override
    public void visitDiv(X86.IDivL div, X86PrinterContext context) throws Exception {
        context.append("\tidivl ");
        visitMemoryAccess(div.getDivider(), context);
        context.append("\n");
    }

    @Override
    public void visitCltd(X86.Cltd cltd, X86PrinterContext context) throws IOException {
        context.printLine("\tcltd");
    }

    @Override
    public void visitXorL(X86.XorL xorL, X86PrinterContext context) throws Exception {
        context.append("\txorl ");
        visitMemoryAccess(xorL.getSource(), context);
        context.append(", ");
        visitMemoryAccess(xorL.getDestination(), context);
        context.append("\n");
    }

    @Override
    public void visitUnknown(X86 command, X86PrinterContext context) throws Exception {
        context.printLine("\t" + command.toString());
    }
}
