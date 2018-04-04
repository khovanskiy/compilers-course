package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class NumberType extends ObjectType {
    public static final NumberType INSTANCE = new NumberType();

    protected NumberType() {
    }

    @Override
    public ObjectType getParentType() {
        return ObjectType.INSTANCE;
    }

    @Override
    public String toString() {
        return "<Number>";
    }
}
