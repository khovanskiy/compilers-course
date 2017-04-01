package ru.ifmo.ctddev.khovanskiy.compilers.common;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.*;
import ru.ifmo.ctddev.khovanskiy.compilers.parser.*;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringReader;

/**
 * @author victor
 */
@Slf4j
public class Main {
    public static void main(String[] args) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(new StringReader("int x = 5;"));
        LanguageLexer lexer = new LanguageLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        LanguageParser parser = new LanguageParser(tokenStream);

        ParseTree tree = parser.compilationUnit();
        System.out.println(tree.toStringTree());
        new LanguageBaseVisitor<ParseTree>(){

        }.visit(tree);
        /*ParseTreeWalker walker = new ParseTreeWalker();
        LanguageListener listener = new LanguageBaseListener() {
            @Override
            public void enterProgram(LanguageParser.ProgramContext ctx) {
                super.enterProgram(ctx);
                log.info("Enter the program");
            }

            @Override
            public void enterAssignment(LanguageParser.AssignmentContext ctx) {
                super.enterAssignment(ctx);
            }

            @Override
            public void exitProgram(LanguageParser.ProgramContext ctx) {
                super.exitProgram(ctx);
                log.info("Exit the program");
            }
        };
        walker.walk(listener, tree);*/
        log.info("Hello, World!");
    }
}
