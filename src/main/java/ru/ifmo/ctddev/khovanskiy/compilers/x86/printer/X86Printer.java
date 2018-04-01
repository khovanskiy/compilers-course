package ru.ifmo.ctddev.khovanskiy.compilers.x86.printer;

import ru.ifmo.ctddev.khovanskiy.compilers.x86.Immediate;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.StackPosition;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.X86;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.X86Program;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Register;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Register8;
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
    public void visitRegister8(Register8 register, X86PrinterContext context) throws IOException {
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
    public void visitCmp(X86.Cmp cmp, X86PrinterContext context) throws Exception {
        context.append("\tcmp ");
        visitMemoryAccess(cmp.getLeft(), context);
        context.append(", ");
        visitMemoryAccess(cmp.getRight(), context);
        context.append("\n");
    }

    @Override
    public void visitAndL(X86.AndL andL, X86PrinterContext context) throws Exception {
        context.append("\tandl ");
        visitMemoryAccess(andL.getLeft(), context);
        context.append(", ");
        visitMemoryAccess(andL.getRight(), context);
        context.append("\n");
    }

    @Override
    public void visitOrL(X86.OrL orL, X86PrinterContext context) throws Exception {
        context.append("\torl ");
        visitMemoryAccess(orL.getLeft(), context);
        context.append(", ");
        visitMemoryAccess(orL.getRight(), context);
        context.append("\n");
    }

    @Override
    public void visitXorL(X86.XorL xorL, X86PrinterContext context) throws Exception {
        context.append("\txorl ");
        visitMemoryAccess(xorL.getLeft(), context);
        context.append(", ");
        visitMemoryAccess(xorL.getRight(), context);
        context.append("\n");
    }

//    private void visitSet(String suffix, Register8 register8, X86PrinterContext context) throws Exception {
//        context.append("\tset" + suffix + " ");
//        visitMemoryAccess(register8, context);
//        context.append("\n");
//    }

    @Override
    public void visitSet(X86.Set command, X86PrinterContext context) throws Exception {
        context.append("\tset");
        super.visitSet(command, context);
        context.append(" ");
        visitMemoryAccess(command.getRegister(), context);
        context.append("\n");
    }

    @Override
    public void visitSetG(X86.SetG setG, X86PrinterContext context) throws Exception {
        context.append("g");
//        visitSet("g", setG.getRegister(), context);
    }

    @Override
    public void visitSetGe(X86.SetGe setGe, X86PrinterContext context) throws Exception {
        context.append("ge");
//        visitSet("ge", setGe.getRegister(), context);
    }

    @Override
    public void visitSetL(X86.SetL setL, X86PrinterContext context) throws Exception {
        context.append("l");
//        visitSet("l", setL.getRegister(), context);
    }

    @Override
    public void visitSetLe(X86.SetLe setLe, X86PrinterContext context) throws Exception {
        context.append("le");
//        visitSet("le", setLe.getRegister(), context);
    }

    @Override
    public void visitSetE(X86.SetE setE, X86PrinterContext context) throws Exception {
        context.append("e");
//        visitSet("e", setE.getRegister(), context);
    }

    @Override
    public void visitSetNe(X86.SetNe setNe, X86PrinterContext context) throws Exception {
        context.append("ne");
//        visitSet("ne", setNe.getRegister(), context);
    }

    @Override
    public void visitSetNz(X86.SetNz setNz, X86PrinterContext context) throws IOException {
        context.append("nz");
    }

    @Override
    public void visitUnknown(X86 command, X86PrinterContext context) throws Exception {
        context.printLine("\t" + command.toString());
    }
}
