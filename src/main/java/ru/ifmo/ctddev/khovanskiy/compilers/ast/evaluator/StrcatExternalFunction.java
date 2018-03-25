package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import java.util.ArrayList;
import java.util.List;

public class StrcatExternalFunction extends ExternalFunction {
    @Override
    @SuppressWarnings("unchecked")
    public Object evaluate(Object... args) {
        assert args.length == 2;
        assert args[0] != null && List.class.isInstance(args[0]);
        assert args[1] != null && List.class.isInstance(args[1]);
        List lhs = (List) args[0];
        List rhs = (List) args[1];
        List result = new ArrayList(lhs.size() + rhs.size());
        result.addAll(lhs);
        result.addAll(rhs);
        return result;
    }
}
