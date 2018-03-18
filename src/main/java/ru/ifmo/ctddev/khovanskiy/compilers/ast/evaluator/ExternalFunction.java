package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

public abstract class ExternalFunction extends MyFunction {
    public abstract Object evaluate(Object... args) throws Exception;
}
