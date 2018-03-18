package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

public class StrmakeExternalFunction extends ExternalFunction {
    @Override
    public Object evaluate(Object... args) {
        assert args.length == 2;
        assert args[0] != null && Integer.class.isInstance(args[0]);
        assert args[1] != null && Character.class.isInstance(args[1]);
        int length = (int) args[0];
        char defaultValue = (char) args[1];
        char[] str = new char[length];
        for (int i = 0; i < length; ++i) {
            str[i] = defaultValue;
        }
        return str;
    }
}
