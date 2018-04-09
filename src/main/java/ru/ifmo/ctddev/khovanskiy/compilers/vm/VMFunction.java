package ru.ifmo.ctddev.khovanskiy.compilers.vm;

import lombok.Getter;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.ConcreteType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class VMFunction {
    private final String name;
    private final int argumentsCount;
    private final List<ConcreteType> types;
    private final ConcreteType returnType;
    private final List<VM> commands;

    public VMFunction(final String name,
                      final int argumentsCount,
                      final List<ConcreteType> types,
                      final ConcreteType returnType) {
        this.name = name;
        this.argumentsCount = argumentsCount;
        this.types = types;
        this.returnType = returnType;
        this.commands = new ArrayList<>();
    }
}
