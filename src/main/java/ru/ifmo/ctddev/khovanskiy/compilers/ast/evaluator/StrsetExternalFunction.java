package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

public class StrsetExternalFunction extends ExternalFunction {
    @Override
    public Object evaluate(Object... args) {
        assert args.length == 3;
        assert args[0] != null && char[].class.isInstance(args[0]);
        assert args[1] != null && Integer.class.isInstance(args[1]);
        assert args[2] != null && Character.class.isInstance(args[2]);
        char[] str = (char[]) args[0];
        int index = (int) args[1];
        char character = (char) args[2];
        str[index] = character;
        return null;
    }
}
