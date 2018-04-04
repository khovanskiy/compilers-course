package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type;

import lombok.Getter;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public abstract class ConcreteType extends Type {
    public ConcreteType getParentType() {
        return null;
    }
}
