package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference;

import lombok.Getter;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class PrimitiveType extends ConcreteType {
    private final VALUE value;

    public PrimitiveType(final VALUE value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof PrimitiveType && this.value == ((PrimitiveType) obj).value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public enum VALUE {
        ARRAY,
        CHAR,
        INTEGER,
        POINTER,
        VOID,
        ANY,
        UNKNOWN
    }
}
