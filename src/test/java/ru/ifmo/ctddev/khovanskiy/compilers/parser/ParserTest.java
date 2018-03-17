package ru.ifmo.ctddev.khovanskiy.compilers.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;
import ru.ifmo.ctddev.khovanskiy.compilers.BaseTest;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.printer.ASTPrinter;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.printer.PrinterContext;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

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
        ANTLRInputStream input = new ANTLRInputStream(new FileReader(path.toFile()));
        LanguageLexer lexer = new LanguageLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        LanguageParser parser = new LanguageParser(tokenStream);

        final AST.CompilationUnit ast = parser.compilationUnit().ast;
        final ASTPrinter printer = new ASTPrinter();
        final Writer writer = new PrintWriter(System.out);
        printer.visitCompilationUnit(ast, new PrinterContext(writer));
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
