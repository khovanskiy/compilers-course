package ru.ifmo.ctddev.khovanskiy.compilers.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;
import ru.ifmo.ctddev.khovanskiy.compilers.BaseTest;

import java.io.FileReader;
import java.io.IOException;
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void parseFile(Path path) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(new FileReader(path.toFile()));
        LanguageLexer lexer = new LanguageLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        LanguageParser parser = new LanguageParser(tokenStream);

        ParseTree tree = parser.compilationUnit();
        System.out.println(tree.toStringTree());
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
