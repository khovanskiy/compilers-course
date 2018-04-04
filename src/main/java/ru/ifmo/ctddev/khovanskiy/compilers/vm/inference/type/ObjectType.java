package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type;

import lombok.Getter;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class ObjectType extends ConcreteType {
    public static final ObjectType INSTANCE = new ObjectType();

    protected ObjectType() {
    }

    public ObjectType getParentType() {
        return INSTANCE;
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
        return "<Object>";
    }
}
