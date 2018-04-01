package ru.ifmo.ctddev.khovanskiy.compilers.x86.compiler;

import lombok.Getter;
import lombok.Setter;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.MemoryAccess;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.StackPosition;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.X86;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Eax;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Register;

import java.util.*;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class CompilerContext {
    private final List<X86> commands = new ArrayList<>();
    private final Stack<Scope> scopes = new Stack<>();
    //private final Map<Register, RegisterEntry> registers = new HashMap<>();
    private final Stack<Register> registers = new Stack<>();
    private final Stack<MemoryAccess> stack = new Stack<>();

    public void enterScope() {
        scopes.push(new Scope());
    }

    public Scope leaveScope() {
        return scopes.pop();
    }

    public Scope getScope() {
        return scopes.peek();
    }

    public void addCommand(X86 command) {
        commands.add(command);
    }

    public void registerArgument(int id) {
        final Scope scope = getScope();
        final Variable variable = scope.getVariables().computeIfAbsent(id, (k) -> new Variable());
        if (variable.getStackPosition() != null) {
            throw new IllegalStateException("Argument is already allocated on stack");
        }
        final StackPosition stackPosition = new StackPosition(4 + (id + 1) * 4);
        variable.setStackPosition(stackPosition);
    }

    /**
     * https://stackoverflow.com/questions/24173899/writing-to-stack-as-local-variable-in-start-function-x86-asm
     *
     * @param id
     */
    public void registerVariable(int id) {
        final Scope scope = getScope();
        final Variable variable = scope.getVariables().computeIfAbsent(id, (k) -> new Variable());
        if (variable.getStackPosition() != null) {
            throw new IllegalStateException("Variable is already allocated on stack");
        }
        final StackPosition stackPosition = new StackPosition(-4 - scope.getAllocated());
        scope.setAllocated(scope.getAllocated() + 4);
        variable.setStackPosition(stackPosition);
    }

    public MemoryAccess allocate() {
        if (!registers.isEmpty()) {
            stack.push(registers.pop());
            return stack.peek();
        } else {
            final Scope scope = getScope();
            stack.push(new StackPosition(-4 - scope.getAllocated()));
            scope.setAllocated(scope.getAllocated() + 4);
            return stack.peek();
        }
        /*final Register register = allocateRegister(false);
        if (register != null) {
            stack.push(register);
            return stack.peek();
        }
        return allocateRegister(true);*/
    }

//    protected Register allocateRegister(boolean force) {
//        for (Map.Entry<Register, RegisterEntry> entry : registers.entrySet()) {
//            final Register register = entry.getKey();
//            final RegisterEntry registerEntry = entry.getValue();
//            if (registerEntry.isInStack()) {
//                continue;
//            }
//            if (registerEntry.getVariable() != null) {
//                if (force) {
//                    //commands.add(new X86.MovL(register, ))
//                } else {
//                    continue;
//                }
//            }
//            return register;
//        }
//        return null;
//    }

    public MemoryAccess pop() {
        final MemoryAccess memoryAccess = stack.pop();
        if (memoryAccess instanceof Register) {
            registers.push((Register) memoryAccess);
        }
        if (memoryAccess instanceof StackPosition) {
            final Scope scope = getScope();
            scope.setAllocated(scope.getAllocated() - 4);
        }
        return memoryAccess;
    }

    public MemoryAccess get(int id) {
        final Scope scope = getScope();
        final Variable variable = scope.getVariables().get(id);
        if (variable == null) {
            throw new IllegalStateException(String.format("Unknown variable \"%d\"", id));
        }
//        if (variable.getRegister() != null) {
//
//            return variable.getRegister();
//        }
        assert variable.getStackPosition() != null;
        return variable.getStackPosition();
    }

//    @Getter
//    @Setter
//    public static class RegisterEntry {
//        private Variable variable;
//        private boolean inStack;
//    }

    @Getter
    @Setter
    public static class Scope {
        private final List<X86> commands = new ArrayList<>();
        private final Map<Integer, Variable> variables = new HashMap<>();
        private int allocated;
        private int maxAllocated;

        public void addCommand(X86 command) {
            commands.add(command);
        }

        public void setAllocated(int allocated) {
            this.allocated = allocated;
            if (maxAllocated < allocated) {
                maxAllocated = allocated;
            }
        }

        public void move(MemoryAccess lhs, MemoryAccess rhs) {
            if (Register.class.isInstance(lhs) || Register.class.isInstance(rhs)) {
                addCommand(new X86.MovL(lhs, rhs));
            } else {
                addCommand(new X86.MovL(lhs, Eax.INSTANCE));
                addCommand(new X86.MovL(Eax.INSTANCE, rhs));
            }
        }
    }

    @Getter
    @Setter
    public static class Variable {
        private StackPosition stackPosition;
//        private Register register;
    }
}
