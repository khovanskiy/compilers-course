package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference;

import lombok.Getter;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class ConcreteType extends Type {
    private final VALUE value;

    public ConcreteType(final VALUE value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ConcreteType && this.value == ((ConcreteType) obj).getValue();
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public enum VALUE {
        ARRAY,
        INTEGER,
        POINTER,
        VOID,
        UNKNOWN
    }
}
