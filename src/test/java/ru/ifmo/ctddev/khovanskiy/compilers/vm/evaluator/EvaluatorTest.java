package ru.ifmo.ctddev.khovanskiy.compilers.vm.evaluator;

import org.junit.Test;
import ru.ifmo.ctddev.khovanskiy.compilers.BaseTest;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.ExternalFunction;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.ReadExternalFunction;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.WriteExternalFunction;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler.CompilerContext;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler.VMCompiler;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.printer.PrinterContext;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.printer.VMPrinter;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
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
    public void test18() {
        int p = 2;

        int cc = 1;

        while (cc != 0) {
            int q = 2;

            while ((q * q <= p) && (cc != 0)) {
                cc = (p % q != 0) ? 1 : 0;
                q = q + 1;
            }

            if (cc != 0) {
                cc = 0;
            } else {
                p = p + 1;
                cc = 1;
            }
        }
        System.out.println("p = " + p + " cc = " + cc);
    }

    protected void evaluate(String s) {
        runTests(s, "./target/temp", (ast, reader, writer) -> {
            final VMCompiler compiler = new VMCompiler();
            final CompilerContext compilerContext = compiler.compile(ast);

            final VMPrinter printer = new VMPrinter();
            final Writer consoleWriter = new PrintWriter(System.out);
            final List<VM> commands = new ArrayList<>();
            commands.add(new VM.Label("read"));
            commands.add(new VM.InvokeExternal("read", 0));
            commands.add(new VM.IReturn());

            commands.add(new VM.Label("write"));
            commands.add(new VM.InvokeExternal("write", 1));
            commands.add(new VM.Return());

            commands.add(new VM.Label("main"));
            commands.addAll(compilerContext.getVmProgram().getCommands());
            final VMProgram newProgram = new VMProgram(commands);
            printer.visitProgram(newProgram, new PrinterContext(consoleWriter));

            Evaluator evaluator = new Evaluator();
            final Map<String, ExternalFunction> externalFunctions = new HashMap<>();
            externalFunctions.put("read", new ReadExternalFunction(reader, writer));
            externalFunctions.put("write", new WriteExternalFunction(writer));
            evaluator.visitProgram(newProgram, new EvaluatorContext(externalFunctions));
        });
    }
}
