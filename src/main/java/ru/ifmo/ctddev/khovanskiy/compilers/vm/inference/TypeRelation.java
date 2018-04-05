package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference;

import lombok.Getter;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.Type;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class TypeRelation {
    private final Type left;

    private final Type right;

    public TypeRelation(final Type left, final Type right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public int hashCode() {
        return left.hashCode() + right.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof TypeRelation) {
            final TypeRelation another = (TypeRelation) obj;
            return this.left.equals(another.left) && this.right.equals(another.right) || this.left.equals(another.right) && this.right.equals(another.left);
        }
        return false;
    }

    @Override
    public String toString() {
        return left + " == " + right;
    }
}
