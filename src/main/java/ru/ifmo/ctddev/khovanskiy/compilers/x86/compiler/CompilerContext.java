package ru.ifmo.ctddev.khovanskiy.compilers.x86.compiler;

import lombok.Getter;
import lombok.Setter;
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
     * @param id the variable ID
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
     * @return the memory pointer for temporary variable
     */
    public MemoryAccess allocate() {
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
        stack.push(new StackPosition(-4 - scope.getAllocated()));
        scope.setAllocated(scope.getAllocated() + 4);
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
        scope.setAllocated(scope.getAllocated() - 4);
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

    public MemoryAccess get(int id) {
        final Scope scope = getScope();
        final Variable variable = scope.getVariables().get(id);
        if (variable == null) {
            throw new IllegalStateException(String.format("Unknown variable \"%d\"", id));
        }
        assert variable.getStackPosition() != null;
        return variable.getStackPosition();
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
        private StackPosition stackPosition;
//        private Register register;
    }
}
