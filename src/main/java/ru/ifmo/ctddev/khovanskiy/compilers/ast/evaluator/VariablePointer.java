package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import lombok.ToString;

import java.util.Objects;

@ToString
public class VariablePointer extends Pointer {
    private final String name;

    public VariablePointer(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object obj) {
        return VariablePointer.class.isInstance(obj) && Objects.equals(this.name, ((VariablePointer) obj).name);
    }
}
