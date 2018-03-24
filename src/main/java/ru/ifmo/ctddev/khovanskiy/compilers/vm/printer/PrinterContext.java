package ru.ifmo.ctddev.khovanskiy.compilers.vm.printer;

import java.io.IOException;
import java.io.Writer;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class PrinterContext {
    private final Writer writer;
    private int lineNumber;

    public PrinterContext(Writer writer) {
        this.writer = writer;
    }

    public Writer printLine(String s) throws IOException {
        this.writer.write(lineNumber + ":\t" + s + "\n");
        ++lineNumber;
        return this.writer;
    }

    public void flush() throws IOException {
        this.writer.flush();
    }
}
