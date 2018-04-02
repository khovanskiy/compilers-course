package ru.ifmo.ctddev.khovanskiy.compilers.ast.parser;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import ru.ifmo.ctddev.khovanskiy.compilers.BaseTest;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.printer.ASTPrinter;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.printer.PrinterContext;

import java.io.PrintWriter;
import java.io.Writer;

@Slf4j
public class ParserTest extends BaseTest {
    @Test
    @Ignore
    public void testCore() {
        runTests("./compiler-tests/core", "./target/temp", (testCase) -> {
            final ASTPrinter printer = new ASTPrinter();
            final Writer consoleWriter = new PrintWriter(System.out);
            printer.visitCompilationUnit(testCase.getAst(), new PrinterContext(consoleWriter));
        });
    }

    @Test
    @Ignore
    public void testDeepExpressions() {
        runTests("./compiler-tests/deep-expressions", "./target/temp", (testCase) -> {
            final ASTPrinter printer = new ASTPrinter();
            final Writer consoleWriter = new PrintWriter(System.out);
            printer.visitCompilationUnit(testCase.getAst(), new PrinterContext(consoleWriter));
        });
    }

    @Test
    @Ignore
    public void testExpressions() {
        runTests("./compiler-tests/expressions", "./target/temp", (testCase) -> {
            final ASTPrinter printer = new ASTPrinter();
            final Writer consoleWriter = new PrintWriter(System.out);
            printer.visitCompilationUnit(testCase.getAst(), new PrinterContext(consoleWriter));
        });
    }

    @Test
    @Ignore
    public void testPerformance() {
        runTests("./compiler-tests/performance", "./target/temp", (testCase) -> {
            final ASTPrinter printer = new ASTPrinter();
            final Writer consoleWriter = new PrintWriter(System.out);
            printer.visitCompilationUnit(testCase.getAst(), new PrinterContext(consoleWriter));
        });
    }
}
