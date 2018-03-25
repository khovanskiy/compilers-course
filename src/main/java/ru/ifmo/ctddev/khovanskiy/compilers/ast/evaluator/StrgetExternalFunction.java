package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import java.util.List;

public class StrgetExternalFunction extends ExternalFunction {
    @Override
    public Object evaluate(Object... args) {
        assert args.length == 2;
        assert args[0] != null;
        assert List.class.isInstance(args[0]);
        assert args[1] != null;
        assert Integer.class.isInstance(args[1]);
        List str = (List) args[0];
        int index = (int) args[1];
        return str.get(index);
    }
}
