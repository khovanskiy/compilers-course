package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class IntegerType extends NumberType {
    public static final IntegerType INSTANCE = new IntegerType();

    IntegerType() {
    }

    @Override
    public NumberType getParentType() {
        return NumberType.INSTANCE;
    }

    @Override
    public String toString() {
        return "<Int>";
    }
}
