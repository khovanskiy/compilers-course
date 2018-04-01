package ru.ifmo.ctddev.khovanskiy.compilers.x86;

import lombok.Getter;
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

    public StackPosition(int position) {
        this.position = position;
        this.register = Ebp.INSTANCE;
    }
}
