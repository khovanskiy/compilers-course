package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import lombok.Getter;

@Getter
public class Symbol<T> {
    private final T value;

    public Symbol(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if (value instanceof char[]) {
            return new String((char[]) value);
        }
        return super.toString();
    }
}
