package ru.ifmo.ctddev.khovanskiy.compilers.ast.printer;

import java.io.IOException;
import java.io.Writer;

/**
 * Abstract syntax tree printer context
 *
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class ASTPrinterContext {
    private final int depth;
    private Writer writer;

    public ASTPrinterContext(Writer writer) {
        this.writer = writer;
        this.depth = 0;
    }

    ASTPrinterContext(ASTPrinterContext previous) {
        this.writer = previous.writer;
        this.depth = previous.depth + 1;
    }

    /**
     * Appends the string to output
     *
     * @param string the string
     * @since 1.0.0
     */
    public ASTPrinterContext append(String string) {
        try {
            this.writer.append(string);
            return this;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Flushes the content to output
     *
     * @since 1.0.0
     */
    public void flush() {
        try {
            this.writer.flush();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
