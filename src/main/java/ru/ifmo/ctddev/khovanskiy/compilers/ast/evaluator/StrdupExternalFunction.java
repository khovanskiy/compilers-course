package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import java.util.ArrayList;
import java.util.List;

public class StrdupExternalFunction extends ExternalFunction {
    @Override
    @SuppressWarnings("unchecked")
    public Object evaluate(Object... args) {
        assert args.length == 1;
        assert args[0] != null && List.class.isInstance(args[0]);
        List str = (List) args[0];
        return new ArrayList<>(str);
    }
}
