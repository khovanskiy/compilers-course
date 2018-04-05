package ru.ifmo.ctddev.khovanskiy.compilers.vm.evaluator;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import ru.ifmo.ctddev.khovanskiy.compilers.BaseTest;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.ASTEvaluatorTest;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.Pointer;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.Symbol;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler.VMCompiler;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.TypeContext;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.TypeInferencer;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.printer.PrinterContext;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.printer.VMPrinter;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
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
        runTests(s, "./target/temp", (testCase) -> {
            final TypeInferencer typeInferencer = new TypeInferencer();
            final TypeContext typeContext = typeInferencer.inference(testCase.getAst());

            final VMCompiler compiler = new VMCompiler();
            final VMProgram vmProgram = compiler.compile(testCase.getAst(), typeContext);

            final VMPrinter printer = new VMPrinter();
            final Writer consoleWriter = new PrintWriter(System.out);
            printer.visitProgram(vmProgram, new PrinterContext(consoleWriter));

            final VMEvaluator VMEvaluator = new VMEvaluator();
            final Map<Pointer, Symbol> externals = ASTEvaluatorTest.defineExternalFunctions(testCase.getReader(), testCase.getWriter());
            VMEvaluator.evaluate(vmProgram, externals);
        });
    }
}
