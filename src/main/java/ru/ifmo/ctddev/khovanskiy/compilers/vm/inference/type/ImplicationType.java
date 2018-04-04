package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class ImplicationType extends ConcreteType {
    private final Type left;
    private final Type right;

    private ImplicationType(Type left, Type right) {
        this.left = left;
        this.right = right;
    }

    public static ImplicationType of(Type left, Type right) {
        return new ImplicationType(left, right);
    }

    @Override
    public String toString() {
        return "(" + left + " -> " + right + ")";
    }
}
