package ru.ifmo.ctddev.khovanskiy.compilers.vm.visitor;

import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMFunction;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public interface VMVisitor<C> {
    /**
     * Visits the virtual machine program
     *
     * @param vmProgram the program
     * @param context   the context
     * @since 1.0.0
     */
    void visitProgram(VMProgram vmProgram, C context);

    /**
     * Visits the function
     *
     * @param function the function
     * @param context  the context
     * @since 1.0.0
     */
    void visitFunction(VMFunction function, C context);

    /**
     * Visits the abstract command
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitCommand(VM command, C context);

    /**
     * Visits the comment command
     *
     * @param comment the command
     * @param context the context
     * @since 1.0.0
     */
    void visitComment(VM.Comment comment, C context);

    /**
     * Visits the duplication element on the top of stack
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitDup(VM.Dup command, C context);

    /**
     * Visits the abstract storing to variable
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitStore(VM.Store command, C context);

    /**
     * Visits the storing to integer variable
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitIStore(VM.IStore command, C context);

    /**
     * Visits the storing to reference variable
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitAStore(VM.AStore command, C context);

    /**
     * Visits the storing integer element to array
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitIAStore(VM.IAStore command, C context);

    /**
     * Visits the storing reference element to array
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitAAStore(VM.AAStore command, C context);

    /**
     * Visits the abstract loading from variable
     *
     * @param command the command
     * @param context the context
     */
    void visitLoad(VM.Load command, C context);

    /**
     * Visits the loading from integer variable
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitILoad(VM.ILoad command, C context);

    /**
     * Visits the loading from reference variable
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitALoad(VM.ALoad command, C context);

    /**
     * Visits the loading integer element from array
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitIALoad(VM.IALoad command, C context);

    /**
     * Visits the loading reference element from array
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitAALoad(VM.AALoad command, C context);

    /**
     * Visits the label
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitLabel(VM.Label command, C context);

    /**
     * Visits the abstract binary command
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitBinOp(VM.BinOp command, C context);

    /**
     * Visits the abstract const
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitConst(VM.Const command, C context);

    /**
     * Visits the reference const
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitAConst(VM.AConst command, C context);

    /**
     * Visits the "null" reference const
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitAConstNull(VM.AConstNull command, C context);

    /**
     * Visits the integer const
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitIConst(VM.IConst command, C context);

    /**
     * Visits the abstract invoke
     *
     * @param command the command
     * @param context the context
     */
    void visitAbstractInvoke(VM.AbstractInvoke command, C context);

    /**
     * Visits the static function invoke
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitInvokeStatic(VM.InvokeStatic command, C context);

    /**
     * Visits the abstract returning
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitAbstractReturn(VM.AbstractReturn command, C context);

    /**
     * Visits the void returning
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitReturn(VM.Return command, C context);

    /**
     * Visits the integer returning
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitIReturn(VM.IReturn command, C context);

    /**
     * Visits the reference returning
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitAReturn(VM.AReturn command, C context);

    /**
     * Visits the unconditional jump
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitGoto(VM.Goto command, C context);

    /**
     * Visits the positive conditional jump
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitIfTrue(VM.IfTrue command, C context);

    /**
     * Visits the negative conditional jump
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitIfFalse(VM.IfFalse command, C context);

    /**
     * Visits the array creation
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitNewArray(VM.NewArray command, C context);

    /**
     * Visits the unknown command
     *
     * @param command the command
     * @param context the context
     * @since 1.0.0
     */
    void visitUnknown(VM command, C context);
}
