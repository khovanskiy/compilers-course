package ru.ifmo.ctddev.khovanskiy.compilers.x86.compiler;

import lombok.extern.slf4j.Slf4j;
import ru.ifmo.ctddev.khovanskiy.compilers.Compiler;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMFunction;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.ConcreteType;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.ImplicationType;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.visitor.AbstractVMVisitor;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.*;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.*;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Slf4j
public class X86Compiler extends AbstractVMVisitor<CompilerContext> implements Compiler<VMProgram, X86Program> {
    @Override
    public X86Program compile(VMProgram vmProgram) throws Exception {
        final CompilerContext compilerContext = new CompilerContext();
        visitProgram(vmProgram, compilerContext);
        verify(compilerContext);
        return new X86Program(compilerContext.getCommands());
    }

    private static final int REFERENCE_COUNT_OFFSET = 0;
    private static final int REFERENCE_DATA_OFFSET = REFERENCE_COUNT_OFFSET + 4;

    private static final int ARRAY_OFFSET = 4;

    private static final boolean GC = true;

    @Override
    public void visitFunction(final VMFunction function, final CompilerContext compilerContext) throws Exception {
        int maxVariableId = -1;
        for (VM command : function.getCommands()) {
            if (command instanceof VM.IStore) {
                final VM.IStore store = (VM.IStore) command;
                maxVariableId = Math.max(maxVariableId, store.getName());
            } else if (command instanceof VM.AStore) {
                final VM.AStore store = (VM.AStore) command;
                maxVariableId = Math.max(maxVariableId, store.getName());
            } else if (command instanceof VM.ILoad) {
                final VM.ILoad load = (VM.ILoad) command;
                maxVariableId = Math.max(maxVariableId, load.getName());
            } else if (command instanceof VM.ALoad) {
                final VM.ALoad load = (VM.ALoad) command;
                maxVariableId = Math.max(maxVariableId, load.getName());
            }
        }
        int argumentsCount = function.getArgumentsCount();
        int localVariablesCount = maxVariableId - argumentsCount + 1;
        log.info("Function \"{}\" has {} arguments and {} local variables", function.getName(), argumentsCount, localVariablesCount);
        compilerContext.addCommand(new X86.Label(function.getName()));
        compilerContext.addCommand(new X86.PushL(Ebp.INSTANCE));
        compilerContext.addCommand(new X86.MovL(Esp.INSTANCE, Ebp.INSTANCE));

        compilerContext.enterScope(function.getName());
        for (int i = 0; i < argumentsCount; ++i) {
            final ConcreteType type = function.getTypes().get(i);
            compilerContext.registerArgument(i, type);
        }
        for (int i = 0; i < localVariablesCount; ++i) {
            final int id = i + argumentsCount;
            final ConcreteType type = function.getTypes().get(id);
            final StackPosition variable = compilerContext.registerVariable(id, type);
            if (isReferenceType(type)) {
                // null reference by default
//                compilerContext.addCommand(new X86.MovL(new Immediate(0), variable));
                compilerContext.wrapInvoke(scope -> {
                    scope.addCommand(new X86.Call("init_reference"));
                    scope.addCommand(new X86.MovL(Eax.INSTANCE, variable));
                });
            }
        }
        super.visitFunction(function, compilerContext);

//        compilerContext.addCommand(new X86.XorL(Eax.INSTANCE, Eax.INSTANCE));
        compilerContext.getScope().addCommand(new X86.Label(returnLabel(function.getName())));

        if (GC) {
            compilerContext.getScope().addCommand(new X86.PushL(Eax.INSTANCE));
            for (int i = 0; i < localVariablesCount; ++i) {
                final int id = i + argumentsCount;
                final ConcreteType type = function.getTypes().get(id);
                if (isReferenceType(type)) {
                    compilerContext.wrapInvoke(scope -> {
                        final StackPosition variable = compilerContext.get(id);
                        scope.addCommand(new X86.PushL(variable));
                        scope.addCommand(new X86.Call("gc_release"));
                        scope.addCommand(new X86.AddL(new Immediate(4), Esp.INSTANCE));
                    });
                }
            }
            compilerContext.getScope().addCommand(new X86.PopL(Eax.INSTANCE));
        }

        final CompilerContext.Scope scope = compilerContext.leaveScope();
        compilerContext.addCommand(new X86.SubL(new Immediate(scope.getMaxAllocated()), Esp.INSTANCE));
        for (final X86 command : scope.getCommands()) {
            compilerContext.addCommand(command);
        }

        compilerContext.addCommand(new X86.MovL(Ebp.INSTANCE, Esp.INSTANCE));
        compilerContext.addCommand(new X86.PopL(Ebp.INSTANCE));
        compilerContext.addCommand(new X86.Ret());
    }

    protected String returnLabel(final String functionName) {
        return functionName + "_end";
    }

    protected boolean isReferenceType(ConcreteType type) {
        return ImplicationType.class.isInstance(type);
    }

    protected void verify(CompilerContext compilerContext) {
        int count = 0;
        for (X86 command : compilerContext.getCommands()) {
            if (command instanceof X86.PushL) {
                ++count;
            } else if (command instanceof X86.PopL) {
                --count;
            }
        }
//        assert count == 0;
    }

    @Override
    public void visitIStore(VM.IStore iStore, CompilerContext compilerContext) {
        final MemoryAccess temporary = compilerContext.pop();
        final MemoryAccess variable = compilerContext.get(iStore.getName());
        compilerContext.getScope().move(temporary, variable);
    }

    @Override
    public void visitAStore(VM.AStore aStore, CompilerContext compilerContext) {
        MemoryAccess temporary = compilerContext.pop();
        final StackPosition variable = compilerContext.get(aStore.getName());

        if (GC) {
            temporary = gcAssign(temporary, variable, compilerContext);
        }

        compilerContext.getScope().move(temporary, variable);
    }

    public MemoryAccess gcAssign(final MemoryAccess newValue, final StackPosition oldValue, final CompilerContext compilerContext) {
        compilerContext.wrapInvoke(scope -> {
            scope.addCommand(new X86.PushL(oldValue));
            scope.addCommand(new X86.PushL(newValue));
            scope.addCommand(new X86.Call("gc_assign"));
            scope.addCommand(new X86.AddL(new Immediate(8), Esp.INSTANCE));
        });
        return Eax.INSTANCE;
    }

    @Override
    public void visitDup(VM.Dup dup, CompilerContext compilerContext) {
        compilerContext.dup();
    }

    @Override
    public void visitIAStore(VM.IAStore iaStore, CompilerContext compilerContext) {
        final MemoryAccess value = compilerContext.pop();
        final MemoryAccess index = compilerContext.pop();
        final MemoryAccess array = compilerContext.pop();

        // reference
        final MemoryAccess element = computeElementMemory(array, index, compilerContext);
        compilerContext.getScope().move(value, element);
    }

    @Override
    public void visitAAStore(VM.AAStore aaStore, CompilerContext compilerContext) {
        MemoryAccess value = compilerContext.pop();
        final MemoryAccess index = compilerContext.pop();
        final MemoryAccess array = compilerContext.pop();

        final StackPosition element = computeElementMemory(array, index, compilerContext);

        if (GC) {
            compilerContext.getScope().addCommand(new X86.PushL(element.getRegister()));
            value = gcAssign(value, element, compilerContext);
            compilerContext.getScope().addCommand(new X86.PopL(element.getRegister()));
        }

        compilerContext.getScope().move(value, element);
    }

    @Override
    public void visitILoad(VM.ILoad iLoad, CompilerContext compilerContext) {
        final MemoryAccess variable = compilerContext.get(iLoad.getName());
        final MemoryAccess temporary = compilerContext.allocate();
        compilerContext.getScope().move(variable, temporary);
    }

    @Override
    public void visitALoad(VM.ALoad aLoad, CompilerContext compilerContext) {
        final MemoryAccess variable = compilerContext.get(aLoad.getName());
        final MemoryAccess temporary = compilerContext.allocate();
        compilerContext.getScope().move(variable, temporary);
    }

    @Override
    public void visitIALoad(VM.IALoad iaLoad, CompilerContext compilerContext) {
        final MemoryAccess index = compilerContext.pop();
        final MemoryAccess array = compilerContext.pop();

        final MemoryAccess element = computeElementMemory(array, index, compilerContext);

        final MemoryAccess temporary = compilerContext.allocate();
        compilerContext.getScope().move(element, temporary);
    }

    protected StackPosition computeElementMemory(MemoryAccess array, MemoryAccess index, CompilerContext compilerContext) {
        // reference
        compilerContext.getScope().addCommand(new X86.MovL(array, Edx.INSTANCE));
        // reference->data
        compilerContext.getScope().addCommand(new X86.MovL(new StackPosition(REFERENCE_DATA_OFFSET, Edx.INSTANCE), Edx.INSTANCE));

        // index
        compilerContext.getScope().addCommand(new X86.MovL(index, Eax.INSTANCE));
        compilerContext.getScope().addCommand(new X86.ImulL(new Immediate(4), Eax.INSTANCE));

        // reference->data[index]
        compilerContext.getScope().addCommand(new X86.AddL(Eax.INSTANCE, Edx.INSTANCE));

        // reference->data[offset + index]
        compilerContext.getScope().addCommand(new X86.AddL(new Immediate(ARRAY_OFFSET), Edx.INSTANCE));

        return new StackPosition(0, Edx.INSTANCE);
    }

    @Override
    public void visitAALoad(VM.AALoad aaLoad, CompilerContext compilerContext) {
        final MemoryAccess index = compilerContext.pop();
        final MemoryAccess array = compilerContext.pop();

        final MemoryAccess element = computeElementMemory(array, index, compilerContext);

        final MemoryAccess temporary = compilerContext.allocate();
        compilerContext.getScope().move(element, temporary);
    }

    @Override
    public void visitLabel(VM.Label label, CompilerContext compilerContext) {
        compilerContext.getScope().addCommand(new X86.Label(label.getName()));
    }

    @Override
    public void visitBinOp(VM.BinOp binOp, CompilerContext compilerContext) {
        switch (binOp.getOperator()) {
            case "+":
                visitIAdd(compilerContext);
                break;
            case "-":
                visitISub(compilerContext);
                break;
            case "*":
                visitIMul(compilerContext);
                break;
            case "/":
                visitIDiv(compilerContext);
                break;
            case "%":
                visitIRem(compilerContext);
                return;
            case "&&":
                visitLogical(binOp, compilerContext);
                return;
            case "!!":
            case "||":
                visitLogical(binOp, compilerContext);
                return;
            default:
                visitCompare(binOp, compilerContext);
        }
    }

    public void visitISub(CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.SubL(rhs, Eax.INSTANCE));
        final MemoryAccess result = compilerContext.allocate();
        compilerContext.getScope().move(Eax.INSTANCE, result);
    }

    public void visitIAdd(CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.AddL(rhs, Eax.INSTANCE));
        final MemoryAccess result = compilerContext.allocate();
        compilerContext.getScope().move(Eax.INSTANCE, result);
    }

    public void visitIMul(CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.ImulL(rhs, Eax.INSTANCE));
        final MemoryAccess result = compilerContext.allocate();
        compilerContext.getScope().move(Eax.INSTANCE, result);
    }

    public void visitIDiv(CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.Cltd());
        compilerContext.getScope().addCommand(new X86.IDivL(rhs));
        final MemoryAccess result = compilerContext.allocate();
        compilerContext.getScope().move(Eax.INSTANCE, result);
    }

    public void visitIRem(CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.Cltd());
        compilerContext.getScope().addCommand(new X86.IDivL(rhs));
        final MemoryAccess result = compilerContext.allocate();
        compilerContext.getScope().move(Edx.INSTANCE, result);
    }

    private void visitCompare(VM.BinOp binOp, CompilerContext compilerContext) {
        //Mov (y, eax); Binop ("^", edx, edx); Binop ("cmp", x, eax); Set (op, "%dl"); Mov (edx, s)
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.XorL(Edx.INSTANCE, Edx.INSTANCE));
        compilerContext.getScope().addCommand(new X86.Cmp(rhs, Eax.INSTANCE));
        switch (binOp.getOperator()) {
            case ">":
                visitG(compilerContext);
                break;
            case ">=":
                visitGe(compilerContext);
                break;
            case "<":
                visitL(compilerContext);
                break;
            case "<=":
                visitLe(compilerContext);
                break;
            case "==":
                visitEq(compilerContext);
                break;
            case "!=":
                visitNe(compilerContext);
                break;
            default:
                throw new UnsupportedOperationException(binOp.getOperator());
        }
        final MemoryAccess result = compilerContext.allocate();
        compilerContext.getScope().move(Edx.INSTANCE, result);
    }

    private void visitG(CompilerContext compilerContext) {
        compilerContext.getScope().addCommand(new X86.SetG(Dl.INSTANCE));
    }

    private void visitGe(CompilerContext compilerContext) {
        compilerContext.getScope().addCommand(new X86.SetGe(Dl.INSTANCE));
    }

    private void visitL(CompilerContext compilerContext) {
        compilerContext.getScope().addCommand(new X86.SetL(Dl.INSTANCE));
    }

    private void visitLe(CompilerContext compilerContext) {
        compilerContext.getScope().addCommand(new X86.SetLe(Dl.INSTANCE));
    }

    private void visitEq(CompilerContext compilerContext) {
        compilerContext.getScope().addCommand(new X86.SetE(Dl.INSTANCE));
    }

    private void visitNe(CompilerContext compilerContext) {
        compilerContext.getScope().addCommand(new X86.SetNe(Dl.INSTANCE));
    }

    private void visitLogical(VM.BinOp binOp, CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().addCommand(new X86.XorL(Eax.INSTANCE, Eax.INSTANCE));
        compilerContext.getScope().addCommand(new X86.XorL(Edx.INSTANCE, Edx.INSTANCE));
        compilerContext.getScope().addCommand(new X86.Cmp(new Immediate(0), lhs));
        compilerContext.getScope().addCommand(new X86.SetNz(Dl.INSTANCE));
        compilerContext.getScope().addCommand(new X86.Cmp(new Immediate(0), rhs));
        compilerContext.getScope().addCommand(new X86.SetNz(Al.INSTANCE));

        switch (binOp.getOperator()) {
            case "&&":
                visitAnd(compilerContext);
                break;
            case "!!":
            case "||":
                visitOr(compilerContext);
                break;
        }
        final MemoryAccess result = compilerContext.allocate();
        compilerContext.getScope().move(Eax.INSTANCE, result);
    }

    private void visitAnd(CompilerContext compilerContext) {
        compilerContext.getScope().addCommand(new X86.AndL(Edx.INSTANCE, Eax.INSTANCE));
    }

    private void visitOr(CompilerContext compilerContext) {
        compilerContext.getScope().addCommand(new X86.OrL(Edx.INSTANCE, Eax.INSTANCE));
    }

    @Override
    public void visitAConstNull(VM.AConstNull aConstNull, CompilerContext compilerContext) {
        final MemoryAccess temporary = compilerContext.allocate();
        compilerContext.getScope().move(new Immediate(0), temporary);
    }

    @Override
    public void visitIConst(VM.IConst iConst, CompilerContext compilerContext) {
        final MemoryAccess temporary = compilerContext.allocate();
        compilerContext.getScope().move(new Immediate(iConst.getValue()), temporary);
    }

    @Override
    public void visitInvokeStatic(VM.InvokeStatic call, CompilerContext compilerContext) {
        assert call.getArgumentsCount() <= compilerContext.getStack().size();

        compilerContext.wrapInvoke(scope -> {
            for (int i = 0; i < call.getArgumentsCount(); ++i) {
                MemoryAccess memoryAccess = compilerContext.pop();
                scope.addCommand(new X86.PushL(memoryAccess));
            }
            scope.addCommand(new X86.Call(call.getName()));
            scope.addCommand(new X86.AddL(new Immediate(4 * call.getArgumentsCount()), Esp.INSTANCE));
        });


        if (!call.getName().equals("write")) { // todo: if returns void type
            final MemoryAccess temporary = compilerContext.allocate();
            compilerContext.getScope().move(Eax.INSTANCE, temporary);
        }
    }

    @Override
    public void visitAbstractReturn(VM.AbstractReturn command, CompilerContext compilerContext) throws Exception {
        super.visitAbstractReturn(command, compilerContext);
        compilerContext.getScope().addCommand(new X86.Jmp(returnLabel(compilerContext.getScope().getName())));
    }

    @Override
    public void visitReturn(VM.Return vmReturn, CompilerContext compilerContext) {
        compilerContext.getScope().addCommand(new X86.XorL(Eax.INSTANCE, Eax.INSTANCE));
    }

    @Override
    public void visitIReturn(VM.IReturn iReturn, CompilerContext compilerContext) {
        final MemoryAccess temporary = compilerContext.pop();
        compilerContext.getScope().move(temporary, Eax.INSTANCE);
    }

    @Override
    public void visitAReturn(VM.AReturn aReturn, CompilerContext compilerContext) {
        final MemoryAccess temporary = compilerContext.pop();
        compilerContext.getScope().move(temporary, Eax.INSTANCE);
        gcReturn(Eax.INSTANCE, compilerContext);
    }

    protected void gcReturn(MemoryAccess temporary, CompilerContext compilerContext) {
        compilerContext.wrapInvoke(scope -> {
            scope.addCommand(new X86.PushL(temporary));
            scope.addCommand(new X86.Call("gc_return"));
            scope.addCommand(new X86.AddL(new Immediate(4), Esp.INSTANCE));
        });
    }

    @Override
    public void visitGoto(VM.Goto vmGoto, CompilerContext compilerContext) {
        compilerContext.getScope().addCommand(new X86.Jmp(vmGoto.getLabel()));
    }

    @Override
    public void visitIfTrue(VM.IfTrue ifTrue, CompilerContext compilerContext) {
        final MemoryAccess memoryAccess = compilerContext.pop();
        compilerContext.getScope().addCommand(new X86.Cmp(new Immediate(0), memoryAccess));
        compilerContext.getScope().addCommand(new X86.Jnz(ifTrue.getLabel()));
    }

    @Override
    public void visitIfFalse(VM.IfFalse ifFalse, CompilerContext compilerContext) {
        final MemoryAccess memoryAccess = compilerContext.pop();
        compilerContext.getScope().addCommand(new X86.Cmp(new Immediate(0), memoryAccess));
        compilerContext.getScope().addCommand(new X86.Jz(ifFalse.getLabel()));
    }

    @Override
    public void visitNewArray(VM.NewArray newArray, CompilerContext compilerContext) {
//        compilerContext.dup();
        MemoryAccess sizeMemory = compilerContext.pop();

        compilerContext.getScope().move(sizeMemory, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.ImulL(new Immediate(4), Eax.INSTANCE));
        compilerContext.getScope().addCommand(new X86.AddL(new Immediate(ARRAY_OFFSET), Eax.INSTANCE));

        compilerContext.wrapInvoke(scope -> {
            scope.addCommand(new X86.PushL(Eax.INSTANCE));
            scope.addCommand(new X86.Call("new_reference"));
            scope.addCommand(new X86.AddL(new Immediate(4), Esp.INSTANCE));
        });

//        sizeMemory = compilerContext.pop();
//        compilerContext.getScope().addCommand(new X86.MovL(sizeMemory, Edx.INSTANCE));
//        compilerContext.getScope().addCommand(new X86.MovL(new StackPosition(REFERENCE_DATA_OFFSET, Edx.INSTANCE), Edx.INSTANCE));
//        compilerContext.getScope().move(Edx.INSTANCE, new StackPosition(0, Edx.INSTANCE));

        final MemoryAccess result = compilerContext.allocate();
        compilerContext.getScope().move(Eax.INSTANCE, result);
    }

    @Override
    public void visitUnknown(VM vm, CompilerContext compilerContext) {
        throw new UnsupportedOperationException();
    }
}
