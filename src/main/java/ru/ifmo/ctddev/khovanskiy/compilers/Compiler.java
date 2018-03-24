package ru.ifmo.ctddev.khovanskiy.compilers;

public interface Compiler<IN, OUT> {
    OUT compile(IN in) throws Exception;
}
