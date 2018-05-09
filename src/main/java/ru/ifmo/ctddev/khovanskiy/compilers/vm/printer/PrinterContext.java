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

    public PrinterContext append(String s) {
        try {
            this.writer.append(s);
            return this;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PrinterContext printLine(String s) {
        try {
            this.writer.write(lineNumber + ":\t" + s + "\n");
            ++lineNumber;
            return this;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void flush() {
        try {
            this.writer.flush();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
