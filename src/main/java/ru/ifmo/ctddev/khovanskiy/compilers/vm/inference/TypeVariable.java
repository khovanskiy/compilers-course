package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class TypeVariable extends Type {
    private final int id;

    public TypeVariable(final int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof TypeVariable && this.id == ((TypeVariable) obj).id;
    }

    @Override
    public String toString() {
        return "T" + id;
    }
}
