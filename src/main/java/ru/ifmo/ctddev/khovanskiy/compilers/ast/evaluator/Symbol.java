package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Symbol<T> {
    private final T value;

    public Symbol(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final String inner;
        if (value instanceof char[]) {
            inner = new String((char[]) value);
        } else {
            inner = Objects.toString(value);
        }
        return "Symbol(" + inner + ")";
    }
}
