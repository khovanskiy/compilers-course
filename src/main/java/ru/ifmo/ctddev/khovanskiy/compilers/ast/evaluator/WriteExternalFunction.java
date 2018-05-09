package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import java.io.IOException;
import java.io.Writer;

public class WriteExternalFunction extends ExternalFunction {
    private final Writer writer;

    public WriteExternalFunction(Writer writer) {
        this.writer = writer;
    }

    @Override
    public Object evaluate(Object... args) {
        assert args.length == 1;
        assert args[0] != null;
        Object arg = args[0] instanceof Character ? ((int) ((char) args[0])) : args[0];
        String str = arg + "\n";
        try {
            writer.write(str);
            writer.flush();
            return null;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
