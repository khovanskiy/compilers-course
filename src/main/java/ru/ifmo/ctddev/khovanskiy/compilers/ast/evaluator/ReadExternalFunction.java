package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Scanner;

public class ReadExternalFunction extends ExternalFunction {
    private final Scanner scanner;
    private final Writer writer;

    public ReadExternalFunction(Reader reader, Writer writer) {
        this.scanner = new Scanner(reader);
        this.writer = writer;
    }

    @Override
    public Object evaluate(Object... args) {
        assert args.length == 0;
        try {
            writer.append("> ");
            return scanner.nextInt();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
