package ru.ifmo.ctddev.khovanskiy.compilers.x86.printer;

import java.io.IOException;
import java.io.Writer;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class X86PrinterContext {
    private final Writer writer;

    public X86PrinterContext(Writer writer) {
        this.writer = writer;
    }

    public Writer append(String s) throws IOException {
        this.writer.append(s);
        return this.writer;
    }

    public Writer printLine(String s) throws IOException {
        this.writer.write(s + "\n");
        return this.writer;
    }

    public void flush() throws IOException {
        this.writer.flush();
    }
}
