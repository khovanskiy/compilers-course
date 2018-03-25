package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import java.util.List;

public class StrcmpExternalFunction extends ExternalFunction {
    @Override
    public Object evaluate(Object... args) {
        assert args.length == 2;
        assert args[0] != null && List.class.isInstance(args[0]);
        assert args[1] != null && List.class.isInstance(args[1]);
        List lhs = (List) args[0];
        List rhs = (List) args[1];
        int lim = Math.min(lhs.size(), rhs.size());
        int k = 0;
        while (k < lim) {
            if (lhs.get(k) != rhs.get(k)) {
                return cmp((int) lhs.get(k), (int) rhs.get(k));
            }
            k++;
        }
        return cmp(lhs.size(), rhs.size());
    }

    @SuppressWarnings("UseCompareMethod")
    private int cmp(int a, int b) {
        return a > b ? 1 : (a < b ? -1 : 0);
    }
}
