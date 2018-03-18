package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import java.io.Reader;
import java.util.Scanner;

public class ReadExternalFunction extends ExternalFunction {
    private final Scanner scanner;

    public ReadExternalFunction(Reader scanner) {
        this.scanner = new Scanner(scanner);
    }

    @Override
    public Object evaluate(Object... args) throws Exception {
        assert args.length == 0;
        return scanner.nextInt();
    }
}
