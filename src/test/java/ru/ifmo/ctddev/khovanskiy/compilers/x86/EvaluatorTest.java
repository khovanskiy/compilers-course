package ru.ifmo.ctddev.khovanskiy.compilers.x86;

import org.junit.Ignore;
import org.junit.Test;
import ru.ifmo.ctddev.khovanskiy.compilers.BaseTest;
import ru.ifmo.ctddev.khovanskiy.compilers.SystemService;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler.CompilerContext;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler.VMCompiler;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.compiler.X86Compiler;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.printer.X86Printer;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.printer.X86PrinterContext;

import java.io.*;

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

    private SystemService systemService = new SystemService();

    protected void evaluate(String s) {
        runTests(s, "./target/temp", (testCase) -> {
            final VMCompiler compiler = new VMCompiler();
            final CompilerContext compilerContext = compiler.compile(testCase.getAst());

            final VMProgram newProgram = compilerContext.getVmProgram();
//            VMPrinter vmPrinter = new VMPrinter();
//            vmPrinter.visitProgram(newProgram, new PrinterContext(new PrintWriter(System.out)));
            File asmFile = new File("./target/temp", testCase.getTestName() + ".s");
            testCase.getTemporaryFiles().add(asmFile);
            try (FileWriter asmWriter = new FileWriter(asmFile)) {
                X86Compiler x86Compiler = new X86Compiler();
                final X86Program x86Program = x86Compiler.compile(newProgram);
                System.out.println("// ----------------------------------------");
                X86Printer x86Printer = new X86Printer();
                x86Printer.visitProgram(x86Program, new X86PrinterContext(asmWriter));
            }

            File objectFile = new File("./target/temp", testCase.getTestName() + ".o");
            testCase.getTemporaryFiles().add(objectFile);
            systemService.executeForRead(new String[]{"./runtime/compile.sh", "./runtime", "./target/temp/" + testCase.getTestName()}, (inputStream -> {
                try (BufferedReader compilationReader = new BufferedReader(new InputStreamReader(inputStream))) {
                    while (compilationReader.ready()) {
                        System.out.println(compilationReader.readLine());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "";
            }));

            File executableFile = new File("./target/temp", testCase.getTestName());
            testCase.getTemporaryFiles().add(executableFile);
            systemService.executeForRead(new String[]{"./runtime/run.sh", "./target/temp/" + testCase.getTestName(), testCase.getInputFile().toString()}, (inputStream -> {
                try (BufferedReader compilationReader = new BufferedReader(new InputStreamReader(inputStream))) {
                    while (compilationReader.ready()) {
                        String line = compilationReader.readLine();
//                        System.out.println("#" + line);
                        testCase.getWriter().write(line + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "";
            }));
            testCase.getWriter().flush();
        });
    }
}
