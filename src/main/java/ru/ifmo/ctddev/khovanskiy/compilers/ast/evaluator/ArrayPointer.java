package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import lombok.Getter;

@Getter
public class ArrayPointer extends Pointer {
    private final Pointer pointer;
    private final int index;

    public ArrayPointer(Pointer pointer, int index) {
        this.pointer = pointer;
        this.index = index;
    }
}
