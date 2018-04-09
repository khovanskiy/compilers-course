package ru.ifmo.ctddev.khovanskiy.compilers.x86;

import org.junit.Ignore;
import org.junit.Test;
import ru.ifmo.ctddev.khovanskiy.compilers.BaseTest;
import ru.ifmo.ctddev.khovanskiy.compilers.SystemService;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler.VMCompiler;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.TypeContext;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.TypeInferencer;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.compiler.X86Compiler;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.printer.X86Printer;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.printer.X86PrinterContext;

import java.io.*;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class EvaluatorTest extends BaseTest {
    private final SystemService systemService = new SystemService();

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

    protected void evaluate(final String s) {
        runTests(s, "./target/temp", (testCase) -> {
            final TypeInferencer typeInferencer = new TypeInferencer();
            final TypeContext typeContext = typeInferencer.inference(testCase.getAst());

            final VMCompiler compiler = new VMCompiler();
            final VMProgram newProgram = compiler.compile(testCase.getAst(), typeContext);

//            VMPrinter vmPrinter = new VMPrinter();
//            vmPrinter.visitProgram(newProgram, new PrinterContext(new PrintWriter(System.out)));
            final File asmFile = new File("./target/temp", testCase.getTestName() + ".s");
            testCase.getTemporaryFiles().add(asmFile);
            try (FileWriter asmWriter = new FileWriter(asmFile)) {
                final X86Compiler x86Compiler = new X86Compiler();
                final X86Program x86Program = x86Compiler.compile(newProgram);

                final X86Printer x86Printer = new X86Printer();
                x86Printer.visitProgram(x86Program, new X86PrinterContext(asmWriter));
            }

            final File objectFile = new File("./target/temp", testCase.getTestName() + ".o");
            testCase.getTemporaryFiles().add(objectFile);
            systemService.executeForRead(new String[] {"./runtime/compile.sh", "./runtime", "./target/temp/" + testCase.getTestName()}, (inputStream -> {
                try (BufferedReader compilationReader = new BufferedReader(new InputStreamReader(inputStream))) {
                    while (compilationReader.ready()) {
                        System.out.println(compilationReader.readLine());
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                return "";
            }));

            final File executableFile = new File("./target/temp", testCase.getTestName());
            testCase.getTemporaryFiles().add(executableFile);
            systemService.executeForRead(new String[] {"./runtime/run.sh", "./target/temp/" + testCase.getTestName(), testCase.getInputFile().toString()}, (inputStream -> {
                try (BufferedReader compilationReader = new BufferedReader(new InputStreamReader(inputStream))) {
                    while (compilationReader.ready()) {
                        final String line = compilationReader.readLine();
//                        System.out.println("#" + line);
                        testCase.getWriter().write(line + "\n");
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                return "";
            }));
            testCase.getWriter().flush();
        });
    }
}
