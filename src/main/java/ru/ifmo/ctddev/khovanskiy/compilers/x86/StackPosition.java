package ru.ifmo.ctddev.khovanskiy.compilers.x86;

import lombok.Getter;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.ConcreteType;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Ebp;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Register;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class StackPosition extends MemoryAccess {
    private final int position;
    private final Register register;
    private final ConcreteType type;

    public StackPosition(int position, ConcreteType type) {
        this.position = position;
        this.register = Ebp.INSTANCE;
        this.type = type;
    }

    public StackPosition(int position, Register register, ConcreteType type) {
        this.position = position;
        this.register = register;
        this.type = type;
    }

    @Override
    public String toString() {
        return position + "(" + register + ")";
    }
}
