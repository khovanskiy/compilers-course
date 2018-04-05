package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type;

import java.util.Set;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public abstract class Type {
    public abstract boolean isConcreteType();

    public abstract Type substitute(TypeVariable typeVariable, Type substitution);

    public abstract Set<TypeVariable> getTypeVariables();
}
