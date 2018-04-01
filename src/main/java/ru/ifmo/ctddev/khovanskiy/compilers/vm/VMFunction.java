package ru.ifmo.ctddev.khovanskiy.compilers.vm;

import lombok.Getter;

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
    private final List<VM> commands;

    public VMFunction(String name, int argumentsCount) {
        this.name = name;
        this.argumentsCount = argumentsCount;
        this.commands = new ArrayList<>();
    }
}
