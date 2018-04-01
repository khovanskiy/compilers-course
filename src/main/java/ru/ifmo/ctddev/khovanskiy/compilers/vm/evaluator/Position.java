package ru.ifmo.ctddev.khovanskiy.compilers.vm.evaluator;

import lombok.Getter;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class Position {
    private final String functionName;
    private final int lineNumber;

    public Position(String functionName, int lineNumber) {
        this.functionName = functionName;
        this.lineNumber = lineNumber;
    }
}
