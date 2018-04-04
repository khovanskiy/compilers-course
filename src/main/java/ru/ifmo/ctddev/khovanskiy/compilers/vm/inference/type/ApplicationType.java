package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class ApplicationType extends ConcreteType {
    private final Type left;
    private final Type right;

    private ApplicationType(Type left, Type right) {
        this.left = left;
        this.right = right;
    }

    public static ApplicationType of(Type left, Type right) {
        return new ApplicationType(left, right);
    }

    @Override
    public String toString() {
        return "(" + left + " " + right + ")";
    }
}
