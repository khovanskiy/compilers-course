package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import java.util.Arrays;

public class StrdupExternalFunction extends ExternalFunction {
    @Override
    public Object evaluate(Object... args) {
        assert args.length == 1;
        assert args[0] != null && char[].class.isInstance(args[0]);
        char[] str = (char[]) args[0];
        return Arrays.copyOf(str, str.length);
    }
}
