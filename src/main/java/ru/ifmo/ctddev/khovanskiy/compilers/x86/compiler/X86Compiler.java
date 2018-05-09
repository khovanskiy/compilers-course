package ru.ifmo.ctddev.khovanskiy.compilers.x86.compiler;

import lombok.extern.slf4j.Slf4j;
import ru.ifmo.ctddev.khovanskiy.compilers.Compiler;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMFunction;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.*;
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
    public X86Program compile(VMProgram vmProgram) {
        final CompilerContext compilerContext = new CompilerContext();
        visitProgram(vmProgram, compilerContext);
        return new X86Program(compilerContext.getCommands());
    }

    private static final int REFERENCE_COUNT_OFFSET = 0;
    private static final int REFERENCE_DATA_OFFSET = REFERENCE_COUNT_OFFSET + 4;

    private static final int ARRAY_OFFSET = 4;

    private static final boolean GC = true;

    @Override
    public void visitFunction(final VMFunction function, final CompilerContext compilerContext) {
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
                // init local variable with reference holder by default
                compilerContext.wrapInvoke(scope -> {
                    scope.addCommand(new X86.Call("init_reference"));
                    scope.addCommand(new X86.MovL(Eax.INSTANCE, variable));
                });
            }
        }
        super.visitFunction(function, compilerContext);

//        compilerContext.addCommand(new X86.XorL(Eax.INSTANCE, Eax.INSTANCE));
        compilerContext.getScope().addCommand(new X86.Label(getFunctionEndLabel(function.getName())));

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

    protected String getFunctionEndLabel(final String functionName) {
        return functionName + "_end";
    }

    protected boolean isReferenceType(ConcreteType type) {
        return ImplicationType.class.isInstance(type);
    }

    @Override
    public void visitIStore(VM.IStore command, CompilerContext compilerContext) {
        final MemoryAccess temporary = compilerContext.pop();
        final MemoryAccess variable = compilerContext.get(command.getName());
        compilerContext.getScope().move(temporary, variable);
    }

    @Override
    public void visitAStore(VM.AStore command, CompilerContext compilerContext) {
        MemoryAccess temporary = compilerContext.pop();
        final StackPosition variable = compilerContext.get(command.getName());

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
    public void visitDup(VM.Dup command, CompilerContext compilerContext) {
        compilerContext.dup();
    }

    @Override
    public void visitIAStore(VM.IAStore command, CompilerContext compilerContext) {
        final MemoryAccess value = compilerContext.pop();
        final MemoryAccess index = compilerContext.pop();
        final MemoryAccess array = compilerContext.pop();

        // reference
        final MemoryAccess element = computeElementMemory(array, index, IntegerType.INSTANCE, compilerContext);
        compilerContext.getScope().move(value, element);
    }

    @Override
    public void visitAAStore(VM.AAStore command, CompilerContext compilerContext) {
        MemoryAccess value = compilerContext.pop();
        final MemoryAccess index = compilerContext.pop();
        final MemoryAccess array = compilerContext.pop();

        final StackPosition element = computeElementMemory(array, index, ObjectType.INSTANCE, compilerContext);

        if (GC) {
            compilerContext.getScope().addCommand(new X86.PushL(element.getRegister()));
            value = gcAssign(value, element, compilerContext);
            compilerContext.getScope().addCommand(new X86.PopL(element.getRegister()));
        }

        compilerContext.getScope().move(value, element);
    }

    @Override
    public void visitILoad(VM.ILoad command, CompilerContext compilerContext) {
        final MemoryAccess variable = compilerContext.get(command.getName());
        final MemoryAccess temporary = compilerContext.allocate(IntegerType.INSTANCE);
        compilerContext.getScope().move(variable, temporary);
    }

    @Override
    public void visitALoad(VM.ALoad command, CompilerContext compilerContext) {
        final MemoryAccess variable = compilerContext.get(command.getName());
        final MemoryAccess temporary = compilerContext.allocate(ObjectType.INSTANCE);
        compilerContext.getScope().move(variable, temporary);
    }

    @Override
    public void visitIALoad(VM.IALoad command, CompilerContext compilerContext) {
        final MemoryAccess index = compilerContext.pop();
        final MemoryAccess array = compilerContext.pop();

        final MemoryAccess element = computeElementMemory(array, index, IntegerType.INSTANCE, compilerContext);

        final MemoryAccess temporary = compilerContext.allocate(IntegerType.INSTANCE);
        compilerContext.getScope().move(element, temporary);
    }

    /**
     * Gets the element memory pointer and produces instruction to take it
     *
     * @param array           the reference to array
     * @param index           the index of element
     * @param elementType     the type of element
     * @param compilerContext the compiler context
     * @return the element memory pointer
     * @since 1.0.0
     */
    protected StackPosition computeElementMemory(MemoryAccess array, MemoryAccess index, ConcreteType elementType, CompilerContext compilerContext) {
        final int bytes = compilerContext.getSizeByType(elementType);

        // reference
        compilerContext.getScope().addCommand(new X86.MovL(array, Edx.INSTANCE));
        // reference->data
        compilerContext.getScope().addCommand(new X86.MovL(new StackPosition(REFERENCE_DATA_OFFSET, Edx.INSTANCE, ObjectType.INSTANCE), Edx.INSTANCE));

        // index
        compilerContext.getScope().addCommand(new X86.MovL(index, Eax.INSTANCE));
        compilerContext.getScope().addCommand(new X86.ImulL(new Immediate(bytes), Eax.INSTANCE));

        // reference->data[index]
        compilerContext.getScope().addCommand(new X86.AddL(Eax.INSTANCE, Edx.INSTANCE));

        // reference->data[offset + index]
        compilerContext.getScope().addCommand(new X86.AddL(new Immediate(ARRAY_OFFSET), Edx.INSTANCE));

        return new StackPosition(0, Edx.INSTANCE, elementType);
    }

    @Override
    public void visitAALoad(VM.AALoad command, CompilerContext compilerContext) {
        final MemoryAccess index = compilerContext.pop();
        final MemoryAccess array = compilerContext.pop();

        final MemoryAccess element = computeElementMemory(array, index, ObjectType.INSTANCE, compilerContext);

        final MemoryAccess temporary = compilerContext.allocate(ObjectType.INSTANCE);
        compilerContext.getScope().move(element, temporary);
    }

    @Override
    public void visitLabel(VM.Label command, CompilerContext compilerContext) {
        compilerContext.getScope().addCommand(new X86.Label(command.getName()));
    }

    @Override
    public void visitBinOp(VM.BinOp command, CompilerContext compilerContext) {
        switch (command.getOperator()) {
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
                visitLogical(command, compilerContext);
                return;
            case "!!":
            case "||":
                visitLogical(command, compilerContext);
                return;
            default:
                visitCompare(command, compilerContext);
        }
    }

    public void visitISub(CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.SubL(rhs, Eax.INSTANCE));
        final MemoryAccess result = compilerContext.allocate(IntegerType.INSTANCE);
        compilerContext.getScope().move(Eax.INSTANCE, result);
    }

    public void visitIAdd(CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.AddL(rhs, Eax.INSTANCE));
        final MemoryAccess result = compilerContext.allocate(IntegerType.INSTANCE);
        compilerContext.getScope().move(Eax.INSTANCE, result);
    }

    public void visitIMul(CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.ImulL(rhs, Eax.INSTANCE));
        final MemoryAccess result = compilerContext.allocate(IntegerType.INSTANCE);
        compilerContext.getScope().move(Eax.INSTANCE, result);
    }

    public void visitIDiv(CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.Cltd());
        compilerContext.getScope().addCommand(new X86.IDivL(rhs));
        final MemoryAccess result = compilerContext.allocate(IntegerType.INSTANCE);
        compilerContext.getScope().move(Eax.INSTANCE, result);
    }

    public void visitIRem(CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.Cltd());
        compilerContext.getScope().addCommand(new X86.IDivL(rhs));
        final MemoryAccess result = compilerContext.allocate(IntegerType.INSTANCE);
        compilerContext.getScope().move(Edx.INSTANCE, result);
    }

    /**
     * Visits the comparing
     *
     * @param command         the command
     * @param compilerContext the compiler context
     * @since 1.0.0
     */
    private void visitCompare(VM.BinOp command, CompilerContext compilerContext) {
        //Mov (y, eax); Binop ("^", edx, edx); Binop ("cmp", x, eax); Set (op, "%dl"); Mov (edx, s)
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().move(lhs, Eax.INSTANCE);
        compilerContext.getScope().addCommand(new X86.XorL(Edx.INSTANCE, Edx.INSTANCE));
        compilerContext.getScope().addCommand(new X86.Cmp(rhs, Eax.INSTANCE));
        switch (command.getOperator()) {
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
                throw new UnsupportedOperationException(command.getOperator());
        }
        final MemoryAccess result = compilerContext.allocate(IntegerType.INSTANCE);
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

    private void visitLogical(VM.BinOp command, CompilerContext compilerContext) {
        MemoryAccess rhs = compilerContext.pop();
        MemoryAccess lhs = compilerContext.pop();
        compilerContext.getScope().addCommand(new X86.XorL(Eax.INSTANCE, Eax.INSTANCE));
        compilerContext.getScope().addCommand(new X86.XorL(Edx.INSTANCE, Edx.INSTANCE));
        compilerContext.getScope().addCommand(new X86.Cmp(new Immediate(0), lhs));
        compilerContext.getScope().addCommand(new X86.SetNz(Dl.INSTANCE));
        compilerContext.getScope().addCommand(new X86.Cmp(new Immediate(0), rhs));
        compilerContext.getScope().addCommand(new X86.SetNz(Al.INSTANCE));

        switch (command.getOperator()) {
            case "&&":
                visitAnd(compilerContext);
                break;
            case "!!":
            case "||":
                visitOr(compilerContext);
                break;
        }
        final MemoryAccess result = compilerContext.allocate(IntegerType.INSTANCE);
        compilerContext.getScope().move(Eax.INSTANCE, result);
    }

    private void visitAnd(CompilerContext compilerContext) {
        compilerContext.getScope().addCommand(new X86.AndL(Edx.INSTANCE, Eax.INSTANCE));
    }

    private void visitOr(CompilerContext compilerContext) {
        compilerContext.getScope().addCommand(new X86.OrL(Edx.INSTANCE, Eax.INSTANCE));
    }

    @Override
    public void visitAConstNull(VM.AConstNull command, CompilerContext compilerContext) {
        final MemoryAccess temporary = compilerContext.allocate(ObjectType.INSTANCE);
        compilerContext.getScope().move(new Immediate(0), temporary);
    }

    @Override
    public void visitIConst(VM.IConst command, CompilerContext compilerContext) {
        final MemoryAccess temporary = compilerContext.allocate(IntegerType.INSTANCE);
        compilerContext.getScope().move(new Immediate(command.getValue()), temporary);
    }

    @Override
    public void visitInvokeStatic(VM.InvokeStatic command, CompilerContext compilerContext) {
        assert command.getArgumentsCount() <= compilerContext.getStack().size();

        compilerContext.wrapInvoke(scope -> {
            for (int i = 0; i < command.getArgumentsCount(); ++i) {
                MemoryAccess memoryAccess = compilerContext.pop();
                scope.addCommand(new X86.PushL(memoryAccess));
            }
            scope.addCommand(new X86.Call(command.getName()));
            scope.addCommand(new X86.AddL(new Immediate(4 * command.getArgumentsCount()), Esp.INSTANCE));
        });

        final ConcreteType returnType = command.getReturnType();
        if (!VoidType.INSTANCE.equals(returnType)) {
            final MemoryAccess temporary = compilerContext.allocate(returnType);
            compilerContext.getScope().move(Eax.INSTANCE, temporary);
        }
    }

    @Override
    public void visitAbstractReturn(VM.AbstractReturn command, CompilerContext compilerContext) {
        super.visitAbstractReturn(command, compilerContext);
        compilerContext.getScope().addCommand(new X86.Jmp(getFunctionEndLabel(compilerContext.getScope().getName())));
    }

    @Override
    public void visitReturn(VM.Return command, CompilerContext compilerContext) {
        compilerContext.getScope().addCommand(new X86.XorL(Eax.INSTANCE, Eax.INSTANCE));
    }

    @Override
    public void visitIReturn(VM.IReturn command, CompilerContext compilerContext) {
        final MemoryAccess temporary = compilerContext.pop();
        compilerContext.getScope().move(temporary, Eax.INSTANCE);
    }

    @Override
    public void visitAReturn(VM.AReturn command, CompilerContext compilerContext) {
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
    public void visitGoto(VM.Goto command, CompilerContext compilerContext) {
        compilerContext.getScope().addCommand(new X86.Jmp(command.getLabel()));
    }

    @Override
    public void visitIfTrue(VM.IfTrue command, CompilerContext compilerContext) {
        final MemoryAccess memoryAccess = compilerContext.pop();
        compilerContext.getScope().addCommand(new X86.Cmp(new Immediate(0), memoryAccess));
        compilerContext.getScope().addCommand(new X86.Jnz(command.getLabel()));
    }

    @Override
    public void visitIfFalse(VM.IfFalse command, CompilerContext compilerContext) {
        final MemoryAccess memoryAccess = compilerContext.pop();
        compilerContext.getScope().addCommand(new X86.Cmp(new Immediate(0), memoryAccess));
        compilerContext.getScope().addCommand(new X86.Jz(command.getLabel()));
    }

    @Override
    public void visitNewArray(VM.NewArray command, CompilerContext compilerContext) {
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

        final MemoryAccess result = compilerContext.allocate(ObjectType.INSTANCE);
        compilerContext.getScope().move(Eax.INSTANCE, result);
    }

    @Override
    public void visitUnknown(VM command, CompilerContext compilerContext) {
        throw new UnsupportedOperationException();
    }
}
