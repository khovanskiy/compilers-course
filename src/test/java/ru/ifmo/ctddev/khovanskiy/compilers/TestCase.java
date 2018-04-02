package ru.ifmo.ctddev.khovanskiy.compilers;

import lombok.Getter;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class TestCase {
    private final String testName;
    private final AST.CompilationUnit ast;
    private final Reader reader;
    private final Writer writer;
    private final File inputFile;
    private final List<File> temporaryFiles = new ArrayList<>();

    public TestCase(String testName, AST.CompilationUnit ast, Reader reader, Writer writer, File inputFile) {
        this.testName = testName;
        this.ast = ast;
        this.reader = reader;
        this.writer = writer;
        this.inputFile = inputFile;
    }
}
