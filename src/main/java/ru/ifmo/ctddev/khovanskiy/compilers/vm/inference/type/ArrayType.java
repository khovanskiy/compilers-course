package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type;

import lombok.Getter;

import java.util.Collections;
import java.util.Set;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class ArrayType extends ConcreteType {
    public static final ArrayType INSTANCE = new ArrayType();
//    private final Type elementType;

//    protected ArrayType(Type elementType) {
//        this.elementType = elementType;
//    }

//    public static ArrayType of(Type elementType) {
//        return new ArrayType(elementType);
//    }

    public ObjectType getParentType() {
        return ObjectType.INSTANCE;
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this.getClass().equals(obj.getClass());
    }

    @Override
    public String toString() {
        return "Array";
    }

    @Override
    public boolean isConcreteType() {
        return true;
    }

    @Override
    public Type substitute(final TypeVariable typeVariable, final Type substitution) {
        return this;
    }

    @Override
    public Set<TypeVariable> getTypeVariables() {
        return Collections.emptySet();
    }
}
