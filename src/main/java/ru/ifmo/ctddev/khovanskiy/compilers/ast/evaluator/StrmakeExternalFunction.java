package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import java.util.ArrayList;
import java.util.List;

public class StrmakeExternalFunction extends ExternalFunction {
    @Override
    @SuppressWarnings("unchecked")
    public Object evaluate(Object... args) {
        assert args.length == 2;
        assert args[0] != null && Integer.class.isInstance(args[0]);
        assert args[1] != null && Integer.class.isInstance(args[1]);
        int length = (int) args[0];
        int defaultValue = (int) args[1];
        List str = new ArrayList(length);
        for (int i = 0; i < length; ++i) {
            str.add(defaultValue);
        }
        return str;
    }
}
