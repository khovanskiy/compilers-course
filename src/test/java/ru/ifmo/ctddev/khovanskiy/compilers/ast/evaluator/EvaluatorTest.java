package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import ru.ifmo.ctddev.khovanskiy.compilers.BaseTest;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.printer.ASTPrinter;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.printer.PrinterContext;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class EvaluatorTest extends BaseTest {

    @Test
    public void testCore() {
        evaluate("./compiler-tests/core");
    }

    @Test
    public void testDeepExpressions() {
        evaluate("./compiler-tests/deep-expressions");
    }

    @Test
    public void testExpressions() {
        evaluate("./compiler-tests/expressions");
    }

    @Test
    @Ignore
    public void testPerformance() {
        evaluate("./compiler-tests/performance");
    }


    protected void evaluate(String s) {
        runTests(s, "./target/temp", (testName, ast, reader, writer) -> {
            final ASTPrinter printer = new ASTPrinter();
            final Writer consoleWriter = new PrintWriter(System.out);
            printer.visitCompilationUnit(ast, new PrinterContext(consoleWriter));

            final Map<Pointer, Symbol> externals = defineExternalFunctions(reader, writer);
            final EvaluatorContext context = new EvaluatorContext(externals);
            Evaluator evaluator = new Evaluator();
            evaluator.visitCompilationUnit(ast, context);
        });
    }

    public static Map<Pointer, Symbol> defineExternalFunctions(final Reader reader, final Writer writer) {
        final Map<Pointer, Symbol> externals = new HashMap<>();
        // base aliases
        externals.put(new VariablePointer("true"), new Symbol<>(1));
        externals.put(new VariablePointer("false"), new Symbol<>(0));
        // io
        externals.put(new FunctionPointer("read"), new Symbol<>(new ReadExternalFunction(reader, writer)));
        externals.put(new FunctionPointer("write"), new Symbol<>(new WriteExternalFunction(writer)));
        // strings
        externals.put(new FunctionPointer("strlen"), new Symbol<>(new StrlenExternalFunction()));
        externals.put(new FunctionPointer("strget"), new Symbol<>(new StrgetExternalFunction()));
        externals.put(new FunctionPointer("strset"), new Symbol<>(new StrsetExternalFunction()));
        externals.put(new FunctionPointer("strsub"), new Symbol<>(new StrsubExternalFunction()));
        externals.put(new FunctionPointer("strdup"), new Symbol<>(new StrdupExternalFunction()));
        externals.put(new FunctionPointer("strcat"), new Symbol<>(new StrcatExternalFunction()));
        externals.put(new FunctionPointer("strcmp"), new Symbol<>(new StrcmpExternalFunction()));
        externals.put(new FunctionPointer("strmake"), new Symbol<>(new StrmakeExternalFunction()));
        // arrays
        externals.put(new FunctionPointer("arrlen"), new Symbol<>(new ArrlenExternalFunction()));
        externals.put(new FunctionPointer("arrmake"), new Symbol<>(new ArrmakeExternalFunction()));
        externals.put(new FunctionPointer("Arrmake"), new Symbol<>(new DynamicArrmakeExternalFunction()));
        return externals;
    }
}
