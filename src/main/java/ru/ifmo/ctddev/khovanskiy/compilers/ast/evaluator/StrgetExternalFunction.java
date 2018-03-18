package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

public class StrgetExternalFunction extends ExternalFunction {
    @Override
    public Object evaluate(Object... args) {
        assert args.length == 2;
        assert args[0] != null && char[].class.isInstance(args[0]);
        assert args[1] != null && Integer.class.isInstance(args[1]);
        char[] str = (char[]) args[0];
        int index = (int) args[1];
        return str[index];
    }
}
