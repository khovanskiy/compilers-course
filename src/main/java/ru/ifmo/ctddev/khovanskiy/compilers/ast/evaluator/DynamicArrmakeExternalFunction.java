package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import java.util.ArrayList;
import java.util.List;

public class DynamicArrmakeExternalFunction extends ExternalFunction {
    @Override
    public Object evaluate(Object... args) {
        assert args.length == 2;
        assert args[0] != null && Integer.class.isInstance(args[0]);
        assert args[1] == null || Pointer.class.isInstance(args[1]);
        int length = (int) args[0];
        Pointer defaultValue = (Pointer) args[1];
        List<Pointer> arr = new ArrayList<>(length);
        for (int i = 0; i < length; ++i) {
            arr.add(defaultValue);
        }
        return arr;
    }
}
