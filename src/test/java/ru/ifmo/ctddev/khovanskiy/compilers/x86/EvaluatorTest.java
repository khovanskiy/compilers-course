package ru.ifmo.ctddev.khovanskiy.compilers.x86;

import org.junit.Ignore;
import org.junit.Test;
import ru.ifmo.ctddev.khovanskiy.compilers.BaseTest;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.Pointer;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.Symbol;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler.CompilerContext;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler.VMCompiler;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.evaluator.VMEvaluator;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.printer.PrinterContext;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.printer.VMPrinter;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.compiler.X86Compiler;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.printer.X86Printer;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.printer.X86PrinterContext;

import java.io.PrintWriter;
import java.io.Writer;
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
//
//    @Test
//    public void testDeepExpressions() {
//        evaluate("./compiler-tests/deep-expressions");
//    }
//
//    @Test
//    public void testExpressions() {
//        evaluate("./compiler-tests/expressions");
//    }

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

            final VMProgram newProgram = compilerContext.getVmProgram();
            printer.visitProgram(newProgram, new PrinterContext(consoleWriter));

            VMEvaluator VMEvaluator = new VMEvaluator();

            final Map<Pointer, Symbol> externals = ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.EvaluatorTest.defineExternalFunctions(reader, writer);

            VMEvaluator.evaluate(newProgram, externals);

            X86Compiler x86Compiler = new X86Compiler();
            final X86Program x86Program = x86Compiler.compile(newProgram);
            System.out.println("// ----------------------------------------");
            X86Printer x86Printer = new X86Printer();
            x86Printer.visitProgram(x86Program, new X86PrinterContext(consoleWriter));
        });
    }
}
