package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class VoidType extends ObjectType {
    public static final VoidType INSTANCE = new VoidType();

    protected VoidType() {
    }

    @Override
    public ObjectType getParentType() {
        return ObjectType.INSTANCE;
    }

    @Override
    public String toString() {
        return "<Void>";
    }
}
