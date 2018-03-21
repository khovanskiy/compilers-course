package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import java.io.Reader;
import java.io.Writer;
import java.util.Scanner;

public class ReadExternalFunction extends ExternalFunction {
    private final Scanner scanner;
    private final Writer writer;

    public ReadExternalFunction(Reader scanner, Writer writer) {
        this.scanner = new Scanner(scanner);
        this.writer = writer;
    }

    @Override
    public Object evaluate(Object... args) throws Exception {
        assert args.length == 0;
        writer.append("> ");
        return scanner.nextInt();
    }
}
