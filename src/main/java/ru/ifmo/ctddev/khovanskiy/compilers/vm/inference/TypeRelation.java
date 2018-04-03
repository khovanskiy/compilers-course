package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference;

import lombok.Getter;


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
    public String toString() {
        return left + " >= " + right;
    }
}
