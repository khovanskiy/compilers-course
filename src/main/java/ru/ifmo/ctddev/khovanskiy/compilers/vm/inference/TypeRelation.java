package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference;

import lombok.Getter;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.Type;

import java.util.Objects;


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
    public boolean equals(Object obj) {
        return obj instanceof TypeRelation
                && Objects.equals(this.left, ((TypeRelation) obj).left)
                && Objects.equals(this.right, ((TypeRelation) obj).right);
    }

    @Override
    public String toString() {
        return left + " >= " + right;
    }
}
