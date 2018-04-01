package ru.ifmo.ctddev.khovanskiy.compilers.x86;

import lombok.Getter;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class Immediate extends MemoryAccess {
    private final int value;

    public Immediate(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Immediate && this.value == ((Immediate) obj).value;
    }

    @Override
    public String toString() {
        return "$" + value;
    }
}
