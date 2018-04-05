package ru.ifmo.ctddev.khovanskiy.compilers;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.parser.LanguageLexer;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.parser.LanguageParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public abstract class BaseTest {
    protected void runTests(final String testDirectory, final String outputDirectory, TestCaseConsumer consumer) {
        final File inputDirectoryFile = new File(testDirectory);

        final List<File> outputFiles = new ArrayList<>();
        try {
            final Stream<Path> testsStream = getTests(inputDirectoryFile);
            testsStream.forEach(path -> {
                final String testName = path.getFileName().toString().split("\\.")[0];
                log.info("Run test \"{}\"", path);
                final File outputDirectoryFile = new File(outputDirectory);
                if (outputDirectoryFile.mkdirs()) {
                    log.info("Directory \"{}\" is created", outputDirectoryFile);
                }
                final File testFile = path.toFile();
                final File inputFile = new File(path.getParent().toFile(), testName + ".input");
                if (!inputFile.exists()) {
                    try {
                        if (inputFile.createNewFile()) {
                            outputFiles.add(inputFile);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                final File originalFile = new File(path.getParent().toFile(), "orig/" + testName + ".log");
                final File outputFile = new File(outputDirectoryFile, testName + ".log");
                outputFiles.add(outputFile);
                try (final FileReader reader = new FileReader(inputFile);
                     final FileWriter writer = new FileWriter(outputFile)) {
                    final AST.CompilationUnit ast = parseAST(testFile);
                    TestCase testCase = new TestCase(testName, ast, reader, writer, inputFile);
                    consumer.run(testCase);
                    outputFiles.addAll(testCase.getTemporaryFiles());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
//                try (final Scanner orig = new Scanner(new FileReader(originalFile));
//                     final Scanner actual = new Scanner(new FileReader(outputFile))) {
//                    int k = 0;
//                    while (orig.hasNext() && actual.hasNext()) {
//                        final String expected = orig.nextLine();
//                        final String actual2 = actual.nextLine();
//                        Assert.assertEquals("Line #" + k, expected, actual2);
//                    }
//                    boolean extra = orig.hasNext() || actual.hasNext();
//                    if (actual.hasNext()) {
//                        System.out.println("Actual file has extra lines:");
//                        while (actual.hasNext()) {
//                            System.out.println(actual.nextLine());
//                        }
//                    }
//                    if (orig.hasNext()) {
//                        System.out.println("Original file has extra lines:");
//                        while (orig.hasNext()) {
//                            System.out.println(orig.nextLine());
//                        }
//                    }
//                    Assert.assertFalse("There are some extra lines", extra);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
            });
        } finally {
            outputFiles.forEach(file -> {
                if (file.exists() && !file.delete()) {
                    log.warn("Output file \"{}\" can not be deleted", file);
                }
            });
        }
    }

    protected AST.CompilationUnit parseAST(final File file) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(new FileReader(file));
        LanguageLexer lexer = new LanguageLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        LanguageParser parser = new LanguageParser(tokenStream);
        return parser.compilationUnit().ast;
    }

    protected Stream<Path> getTests(final File directory) {
        try {
            return Files.list(directory.toPath())
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".expr"))
                    .sorted();
        } catch (IOException e) {
            return Stream.empty();
        }
    }

    @FunctionalInterface
    public interface TestCaseConsumer {
        void run(TestCase testCase) throws Exception;
    }
}
