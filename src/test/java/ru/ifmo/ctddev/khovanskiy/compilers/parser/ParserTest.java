package ru.ifmo.ctddev.khovanskiy.compilers.parser;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;
import ru.ifmo.ctddev.khovanskiy.compilers.BaseTest;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.*;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.printer.ASTPrinter;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.printer.PrinterContext;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class ParserTest extends BaseTest {

    @Test
    public void testCore() throws IOException {
        getTests("./compiler-tests/core").forEach(path -> {
            System.out.println(path.getFileName());
            try {
                parseFile(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void parseFile(Path path) throws Exception {
        final AST.CompilationUnit ast = parseAST(path.toFile());
        {
            final ASTPrinter printer = new ASTPrinter();
            final Writer writer = new PrintWriter(System.out);
            printer.visitCompilationUnit(ast, new PrinterContext(writer));
        }
        final String name = path.getFileName().toString().split("\\.")[0];
        final File inputFile = new File("./compiler-tests/core/" + name + ".input");
        log.info("Input: " + inputFile.getAbsolutePath());
        try (final FileReader reader = new FileReader(inputFile);
             final FileWriter writer = new FileWriter("test001.output")) {
            final Map<Pointer, Symbol> externals = defineExternalFunctions(reader, writer);
            final EvaluatorContext context = new EvaluatorContext(externals);
            Evaluator evaluator = new Evaluator();
            evaluator.visitCompilationUnit(ast, context);
        }
    }

    private AST.CompilationUnit parseAST(final File file) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(new FileReader(file));
        LanguageLexer lexer = new LanguageLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        LanguageParser parser = new LanguageParser(tokenStream);
        return parser.compilationUnit().ast;
    }

    private Map<Pointer, Symbol> defineExternalFunctions(final FileReader reader, final FileWriter writer) {
        final Map<Pointer, Symbol> externals = new HashMap<>();
        // base aliases
        externals.put(new VariablePointer("true"), new Symbol<>(1));
        externals.put(new VariablePointer("false"), new Symbol<>(0));
        // io
        externals.put(new FunctionPointer("read"), new Symbol<>(new ReadExternalFunction(reader)));
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

    private Stream<Path> getTests(final String directory) {
        try {
            return Files.list(Paths.get(directory))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".expr"))
                    .sorted(Comparator.comparing(Path::toString));
        } catch (IOException e) {
            return Stream.empty();
        }
    }
}
