package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import lombok.ToString;

import java.util.Objects;

@ToString
public class FunctionPointer extends Pointer {
    private final String name;

    public FunctionPointer(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FunctionPointer) {
            return Objects.equals(this.name, ((FunctionPointer) obj).name);
        }
        return false;
    }
}
