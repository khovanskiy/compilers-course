package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

public class StrcatExternalFunction extends ExternalFunction {
    @Override
    public Object evaluate(Object... args) {
        assert args.length == 2;
        assert args[0] != null && char[].class.isInstance(args[0]);
        assert args[1] != null && char[].class.isInstance(args[1]);
        char[] lhs = (char[]) args[0];
        char[] rhs = (char[]) args[1];
        char[] result = new char[lhs.length + rhs.length];
        System.arraycopy(lhs, 0, result, 0, lhs.length);
        System.arraycopy(rhs, 0, result, lhs.length, rhs.length);
        return result;
    }
}
