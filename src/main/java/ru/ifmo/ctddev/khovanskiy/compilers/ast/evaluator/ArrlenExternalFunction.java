package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import java.util.List;

public class ArrlenExternalFunction extends ExternalFunction {
    @Override
    public Object evaluate(Object... args) {
        assert args.length == 1;
        assert args[0] != null && List.class.isInstance(args[0]);
        return ((List) args[0]).size();
    }
}
