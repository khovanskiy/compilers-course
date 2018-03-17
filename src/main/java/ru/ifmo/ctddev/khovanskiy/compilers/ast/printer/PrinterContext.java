package ru.ifmo.ctddev.khovanskiy.compilers.ast.printer;

import java.io.IOException;
import java.io.Writer;

public class PrinterContext {
    private final int depth;
    private PrinterContext previous;
    private Writer writer;

    public PrinterContext(Writer writer) {
        this.writer = writer;
        this.depth = 0;
    }

    public PrinterContext(PrinterContext previous) {
        this.previous = previous;
        this.writer = previous.writer;
        this.depth = previous.depth + 1;
    }

    public Writer append(String s) throws IOException {
        this.writer.append(s);
        return this.writer;
    }

    public void space() throws IOException {
        for (int i = 0; i < depth; ++i) {
            this.writer.append("\t");
        }
    }

    public void flush() throws IOException {
        this.writer.flush();
    }
}
