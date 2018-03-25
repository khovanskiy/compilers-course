package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import java.util.List;

public class StrlenExternalFunction extends ExternalFunction {
    @Override
    public Object evaluate(Object... args) {
        assert args.length == 1;
        assert args[0] != null;
        assert char[].class.isInstance(args[0]) || List.class.isInstance(args[0]);
        if (args[0] instanceof char[]) {
            return ((char[]) args[0]).length;
        }
        return ((List) args[0]).size();
    }
}
