package ru.ifmo.ctddev.khovanskiy.compilers.vm.evaluator;

import org.junit.Ignore;
import org.junit.Test;
import ru.ifmo.ctddev.khovanskiy.compilers.BaseTest;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.Pointer;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.Symbol;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler.CompilerContext;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler.VMCompiler;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.printer.PrinterContext;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.printer.VMPrinter;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
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
        runTests(s, "./target/temp", (ast, reader, writer) -> {
            final VMCompiler compiler = new VMCompiler();
            final CompilerContext compilerContext = compiler.compile(ast);

            final VMPrinter printer = new VMPrinter();
            final Writer consoleWriter = new PrintWriter(System.out);
            final List<VM> commands = new ArrayList<>();

            commands.add(new VM.Label("main"));
            commands.addAll(compilerContext.getVmProgram().getCommands());
            final VMProgram newProgram = new VMProgram(commands);
            printer.visitProgram(newProgram, new PrinterContext(consoleWriter));

            Evaluator evaluator = new Evaluator();

            final Map<Pointer, Symbol> externals = ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.EvaluatorTest.defineExternalFunctions(reader, writer);

            evaluator.visitProgram(newProgram, new EvaluatorContext(externals));
        });
    }
}
