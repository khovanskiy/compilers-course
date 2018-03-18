package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

public class StrcmpExternalFunction extends ExternalFunction {
    @Override
    public Object evaluate(Object... args) {
        assert args.length == 2;
        assert args[0] != null && char[].class.isInstance(args[0]);
        assert args[1] != null && char[].class.isInstance(args[1]);
        char[] lhs = (char[]) args[0];
        char[] rhs = (char[]) args[1];
        int lim = Math.min(lhs.length, rhs.length);
        int k = 0;
        while (k < lim) {
            if (lhs[k] != rhs[k]) {
                return cmp(lhs[k], rhs[k]);
            }
            k++;
        }
        return cmp(lhs.length, rhs.length);
    }

    @SuppressWarnings("UseCompareMethod")
    private int cmp(int a, int b) {
        return a > b ? 1 : (a < b ? -1 : 0);
    }
}
