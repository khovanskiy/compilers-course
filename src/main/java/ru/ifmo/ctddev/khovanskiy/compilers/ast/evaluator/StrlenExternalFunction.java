package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

public class StrlenExternalFunction extends ExternalFunction {
    @Override
    public Object evaluate(Object... args) {
        assert args.length == 1;
        assert args[0] != null && char[].class.isInstance(args[0]);
        return ((char[]) args[0]).length;
    }
}
