package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import java.util.Arrays;

public class StrsubExternalFunction extends ExternalFunction {
    @Override
    public Object evaluate(Object... args) {
        assert args.length == 3;
        assert args[0] != null && char[].class.isInstance(args[0]);
        assert args[1] != null && Integer.class.isInstance(args[1]);
        assert args[2] != null && Integer.class.isInstance(args[2]);
        char[] str = (char[]) args[0];
        int lower = (int) args[1];
        int upper = (int) args[2];
        return Arrays.copyOfRange(str, lower, lower + upper);
    }
}
