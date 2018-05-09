package ru.ifmo.ctddev.khovanskiy.compilers.x86.compiler;

import lombok.Getter;
import lombok.Setter;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.ConcreteType;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.ImplicationType;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.IntegerType;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.ObjectType;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.Immediate;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.MemoryAccess;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.StackPosition;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.X86;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.*;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class CompilerContext {
    private final List<X86> commands = new ArrayList<>();
    private final Stack<Scope> scopes = new Stack<>();
    private final List<RegisterEntry> registers = new ArrayList<>();
    private final Stack<MemoryAccess> stack = new Stack<>();

    public CompilerContext() {
        registers.add(new RegisterEntry(Ebx.INSTANCE));
        registers.add(new RegisterEntry(Ecx.INSTANCE));
        registers.add(new RegisterEntry(Edi.INSTANCE));
        registers.add(new RegisterEntry(Esi.INSTANCE));
    }

    public void enterScope(String name) {
        scopes.push(new Scope(name));
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

    /**
     * Registers the argument at the stack
     *
     * @param id   the argument ID
     * @param type the argument type
     * @return the stack position
     * @since 1.0.0
     */
    public StackPosition registerArgument(int id, final ConcreteType type) {
        final Scope scope = getScope();
        final Variable variable = scope.getVariables().computeIfAbsent(id, (k) -> new Variable(type));
        if (variable.getStackPosition() != null) {
            throw new IllegalStateException("Argument is already allocated on stack");
        }
        final int returnAddressSize = getSizeByType(IntegerType.INSTANCE);
        final int ebpSize = getSizeByType(IntegerType.INSTANCE);
        final StackPosition stackPosition = new StackPosition(ebpSize + returnAddressSize + id * getSizeByType(type), type);
        variable.setStackPosition(stackPosition);
        return stackPosition;
    }

    /**
     * Registers the local variable at the stack
     *
     * @param id   the variable ID
     * @param type the variable type
     * @see <a href="https://stackoverflow.com/questions/24173899/writing-to-stack-as-local-variable-in-start-function-x86-asm">Writing to stack as local variable in _start function (x86 ASM)</a>
     * @since 1.0.0
     */
    public StackPosition registerVariable(int id, final ConcreteType type) {
        final Scope scope = getScope();
        final Variable variable = scope.getVariables().computeIfAbsent(id, (k) -> new Variable(type));
        if (variable.getStackPosition() != null) {
            throw new IllegalStateException("Variable is already allocated on stack");
        }
        final int ebpSize = getSizeByType(IntegerType.INSTANCE);
        final StackPosition stackPosition = new StackPosition(-ebpSize - scope.getAllocated(), type);
        scope.setAllocated(scope.getAllocated() + getSizeByType(type));
        variable.setStackPosition(stackPosition);
        return stackPosition;
    }

    /**
     * Wraps the compilation of function invoke
     *
     * @param consumer the consumer of new scope
     * @since 1.0.0
     */
    public void wrapInvoke(Consumer<Scope> consumer) {
        final Scope scope = getScope();
        final List<Register> stored = new ArrayList<>();
        for (final RegisterEntry entry : registers) {
            if (entry.getUsageCount() > 0) {
                scope.addCommand(new X86.PushL(entry.getRegister()));
                stored.add(entry.getRegister());
            }
        }
        consumer.accept(scope);
        for (int i = stored.size() - 1; i >= 0; --i) {
            final Register register = stored.get(i);
            scope.addCommand(new X86.PopL(register));
        }
    }

    /**
     * Allocate temporary variable
     *
     * @param type the type of allocated memory
     * @return the memory pointer for temporary variable
     */
    public MemoryAccess allocate(ConcreteType type) {
        for (final RegisterEntry entry : registers) {
            if (entry.getUsageCount() > 0) {
                continue;
            }
            if (stack.isEmpty() || stack.peek() instanceof Register) {
                entry.setUsageCount(entry.getUsageCount() + 1);
                stack.push(entry.getRegister());
                return stack.peek();
            }
        }
        final Scope scope = getScope();
        final int ebpSize = getSizeByType(IntegerType.INSTANCE);
        stack.push(new StackPosition(-ebpSize - scope.getAllocated(), type));
        scope.setAllocated(scope.getAllocated() + getSizeByType(type));
        return stack.peek();
    }

    public MemoryAccess pop() {
        final MemoryAccess memoryAccess = stack.pop();
        if (memoryAccess instanceof Register) {
            Register register = (Register) memoryAccess;
            for (final RegisterEntry entry : registers) {
                if (register.equals(entry.getRegister())) {
                    entry.setUsageCount(entry.getUsageCount() - 1);
                    assert entry.getUsageCount() >= 0;
                    break;
                }
            }
        }
        if (memoryAccess instanceof StackPosition) {
            StackPosition current = (StackPosition) memoryAccess;
            popStackPosition(current);
        }
        return memoryAccess;
    }

    private void popStackPosition(final StackPosition current) {
        final Scope scope = getScope();
        if (!stack.isEmpty()) {
            if (stack.peek() instanceof StackPosition) {
                final StackPosition previous = (StackPosition) stack.peek();
                final boolean isDuplicate = previous.getPosition() == current.getPosition();
                if (isDuplicate) {
                    return;
                }
            }
        }
        scope.setAllocated(scope.getAllocated() - getSizeByType(current.getType()));
    }

    public void dup() {
        MemoryAccess memoryAccess = stack.peek();
        if (memoryAccess instanceof Register) {
            Register register = (Register) memoryAccess;
            for (final RegisterEntry entry : registers) {
                if (register.equals(entry.getRegister())) {
                    entry.setUsageCount(entry.getUsageCount() + 1);
                    assert entry.getUsageCount() >= 0;
                    break;
                }
            }
        }
        stack.push(memoryAccess);
    }

    public StackPosition get(int id) {
        final Scope scope = getScope();
        final Variable variable = scope.getVariables().get(id);
        if (variable == null) {
            throw new IllegalStateException(String.format("Function \"\": unknown variable \"%d\"", id));
        }
        assert variable.getStackPosition() != null;
        return variable.getStackPosition();
    }

    /**
     * Gets size in bytes by type
     *
     * @param type the type
     * @return the size in bytes
     * @since 1.1.0
     */
    public int getSizeByType(ConcreteType type) {
        if (type instanceof IntegerType) {
            return 4;
        } else if (type instanceof ImplicationType) {
            return 4;
        } else if (type instanceof ObjectType) {
            return 4;
        } else {
            throw new IllegalArgumentException("Unknown size of type: " + type);
        }
    }

    @Getter
    @Setter
    public static class RegisterEntry {
        private final Register register;
        private int usageCount;

        public RegisterEntry(Register register) {
            this.register = register;
        }
    }

    @Getter
    @Setter
    public static class Scope {
        private final List<X86> commands = new ArrayList<>();
        private final Map<Integer, Variable> variables = new HashMap<>();
        private final String name;
        private int allocated;
        private int maxAllocated;

        public Scope(String name) {
            this.name = name;
        }

        public void addCommand(X86 command) {
            commands.add(command);
        }

        public void setAllocated(int allocated) {
            this.allocated = allocated;
            if (maxAllocated < allocated) {
                maxAllocated = allocated;
            }
        }

        public boolean isVariable(MemoryAccess memoryAccess) {
            return memoryAccess instanceof StackPosition &&
                    variables.values().stream().map(Variable::getStackPosition).anyMatch(memoryAccess::equals);
        }

        public void move(MemoryAccess source, MemoryAccess destination) {
            if (Register.class.isInstance(source) || Register.class.isInstance(destination)) {
                addCommand(new X86.MovL(source, destination));
            } else if (Immediate.class.isInstance(source)) {
                addCommand(new X86.MovL(source, destination));
            } else {
                addCommand(new X86.MovL(source, Eax.INSTANCE));
                addCommand(new X86.MovL(Eax.INSTANCE, destination));
            }
        }
    }

    @Getter
    @Setter
    public static class Variable {
        private final ConcreteType type;
        private StackPosition stackPosition;

        public Variable(ConcreteType type) {
            this.type = type;
        }
    }
}
