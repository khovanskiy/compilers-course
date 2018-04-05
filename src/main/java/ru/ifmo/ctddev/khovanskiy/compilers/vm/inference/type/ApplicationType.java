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
public class ApplicationType extends ConcreteType {
    private final Type left;
    private final Type right;

    private ApplicationType(final Type left, final Type right) {
        this.left = left;
        this.right = right;
    }

    public static ApplicationType of(final Type left, final Type right) {
        return new ApplicationType(left, right);
    }

    @Override
    public String toString() {
        return "(" + left + " " + right + ")";
    }

    @Override
    public boolean isConcreteType() {
        return left.isConcreteType() && right.isConcreteType();
    }

    @Override
    public Type substitute(final TypeVariable typeVariable, final Type substitution) {
        return new ApplicationType(left.substitute(typeVariable, substitution), right.substitute(typeVariable, substitution)).simplify();
    }

    public Type simplify() {
        if (left instanceof ImplicationType && ((ImplicationType) left).getLeft().isConcreteType()) {
            return ((ImplicationType) left).getRight();
        }
        return this;
    }

    @Override
    public Set<TypeVariable> getTypeVariables() {
        return SetUtils.union(left.getTypeVariables(), right.getTypeVariables());
    }
}
