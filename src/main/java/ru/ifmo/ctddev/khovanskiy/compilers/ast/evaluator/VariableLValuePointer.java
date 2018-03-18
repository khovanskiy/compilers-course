package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

public class VariableLValuePointer extends LValuePointer {
    private final String name;

    public VariableLValuePointer(String name) {
        this.name = name;
    }
}
