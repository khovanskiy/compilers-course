package ru.ifmo.ctddev.khovanskiy.compilers.x86.register;

import ru.ifmo.ctddev.khovanskiy.compilers.x86.MemoryAccess;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public abstract class Register extends MemoryAccess {
    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.getClass().isInstance(obj);
    }

    @Override
    public String toString() {
        return "%" + this.getClass().getSimpleName().toLowerCase();
    }
}
