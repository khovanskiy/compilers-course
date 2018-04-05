package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.SetUtils;

import java.util.Set;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class ImplicationType extends ConcreteType {
    private final Type left;
    private final Type right;

    private ImplicationType(final Type left, final Type right) {
        this.left = left;
        this.right = right;
    }

    public static ImplicationType of(final Type left, final Type right) {
        return new ImplicationType(left, right);
    }

    @Override
    public String toString() {
        return "(" + left + " -> " + right + ")";
    }

    @Override
    public boolean isConcreteType() {
        return left.isConcreteType() && right.isConcreteType();
    }

    @Override
    public ImplicationType substitute(final TypeVariable typeVariable, final Type substitution) {
        return new ImplicationType(left.substitute(typeVariable, substitution), right.substitute(typeVariable, substitution));
    }

    @Override
    public Set<TypeVariable> getTypeVariables() {
        return SetUtils.union(left.getTypeVariables(), right.getTypeVariables());
    }
}
