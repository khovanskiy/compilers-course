package ru.ifmo.ctddev.khovanskiy.compilers.vm.evaluator;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import ru.ifmo.ctddev.khovanskiy.compilers.BaseTest;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.ASTEvaluatorTest;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.Pointer;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.Symbol;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler.CompilerContext;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler.VMCompiler;

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
            final VMCompiler compiler = new VMCompiler();
            final CompilerContext compilerContext = compiler.compile(testCase.getAst());

            final VMProgram newProgram = compilerContext.getVmProgram();

//            final VMPrinter printer = new VMPrinter();
//            final Writer consoleWriter = new PrintWriter(System.out);
//            printer.visitProgram(newProgram, new PrinterContext(consoleWriter));

            final VMEvaluator VMEvaluator = new VMEvaluator();
            final Map<Pointer, Symbol> externals = ASTEvaluatorTest.defineExternalFunctions(testCase.getReader(), testCase.getWriter());
            VMEvaluator.evaluate(newProgram, externals);
        });
    }
}
