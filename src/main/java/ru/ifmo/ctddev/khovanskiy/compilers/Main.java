package ru.ifmo.ctddev.khovanskiy.compilers;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.cli.*;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.parser.LanguageLexer;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.parser.LanguageParser;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.printer.ASTPrinter;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.printer.PrinterContext;

import java.io.*;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Slf4j
public class Main {
    public static void main(String[] args) {
        new Main().run(args);
    }

    public void run(String[] args) {
        Options options = new Options()
                .addOption(Option.builder("i").longOpt("interpreter").desc("Run AST interpreter").build())
                .addOption(Option.builder("s").longOpt("stack").desc("Run Virtual Machine").build())
                .addOption(Option.builder("h").longOpt("help").desc("Show help").build());
        try {
            DefaultParser defaultParser = new DefaultParser();
            final CommandLine commandLine = defaultParser.parse(options, args);
            if (commandLine.hasOption("h")) {
                showHelp(options);
                return;
            }
            try (final Reader reader = new BufferedReader(new InputStreamReader(System.in));
                 final Writer writer = new BufferedWriter(new OutputStreamWriter(System.out))) {
                if (commandLine.hasOption("i")) {
                    runInterpreter(reader, writer);
                }
            }
        } catch (ParseException e) {
            showHelp(options);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void runInterpreter(Reader reader, Writer writer) throws Exception {
        AST.CompilationUnit ast = parseAST(reader);
        ASTPrinter astPrinter = new ASTPrinter();
        astPrinter.visitCompilationUnit(ast, new PrinterContext(writer));
    }

    protected void showHelp(final Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("X86-GAS Compiler", options);
    }

    protected AST.CompilationUnit parseAST(Reader reader) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(reader);
        LanguageLexer lexer = new LanguageLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        LanguageParser parser = new LanguageParser(tokenStream);
        return parser.compilationUnit().ast;
    }
}
