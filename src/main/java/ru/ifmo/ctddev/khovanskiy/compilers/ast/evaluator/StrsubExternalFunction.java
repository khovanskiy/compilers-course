package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import java.util.ArrayList;
import java.util.List;

public class StrsubExternalFunction extends ExternalFunction {
    @Override
    @SuppressWarnings("unchecked")
    public Object evaluate(Object... args) {
        assert args.length == 3;
        assert args[0] != null && List.class.isInstance(args[0]);
        assert args[1] != null && Integer.class.isInstance(args[1]);
        assert args[2] != null && Integer.class.isInstance(args[2]);
        List str = (List) args[0];
        int lower = (int) args[1];
        int upper = (int) args[2];
        return new ArrayList<>(str.subList(lower, lower + upper));
    }
}
