package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

public class ArrlenExternalFunction extends ExternalFunction {
    @Override
    public Object evaluate(Object... args) {
        assert args.length == 1;
        assert args[0] != null && int[].class.isInstance(args[0]);
        return ((int[]) args[0]).length;
    }
}
