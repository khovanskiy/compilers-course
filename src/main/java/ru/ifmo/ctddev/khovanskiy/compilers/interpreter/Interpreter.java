package ru.ifmo.ctddev.khovanskiy.compilers.interpreter;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import ru.ifmo.ctddev.khovanskiy.compilers.AST;
import ru.ifmo.ctddev.khovanskiy.compilers.Parser;
import ru.ifmo.ctddev.khovanskiy.compilers.parser.LanguageBaseVisitor;
import ru.ifmo.ctddev.khovanskiy.compilers.parser.LanguageLexer;
import ru.ifmo.ctddev.khovanskiy.compilers.parser.LanguageParser;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * @author victor
 */
@Slf4j
public class Interpreter extends LanguageBaseVisitor<ParseTree> {
    //private Map<String, >
    private final Scanner reader;
    private final PrintWriter writer;
    private Map<String, Object> memory = new HashMap<>();

    public Interpreter(FileReader reader, Writer writer) {
        this.reader = new Scanner(reader);
        this.writer = new PrintWriter(writer);
    }

    public static void main(String[] args) throws IOException {
        String number = "test022";
        FileReader source = new FileReader(new File("compiler-tests/core/" + number + ".expr"));

        //StringReader source = new StringReader("if 11 > 5 then write(1) elif 2 > 5 then write(2) fi");
        ANTLRInputStream input = new ANTLRInputStream(source);
        LanguageLexer lexer = new LanguageLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        LanguageParser parser = new LanguageParser(tokenStream);
        LanguageParser.CompilationUnitContext tree = parser.compilationUnit();
        AST.CompilationUnit compilationUnit = new Parser().visitCompilationUnit(tree);
        //System.out.println(compilationUnit);
        FileReader reader = new FileReader(new File("compiler-tests/core/" + number + ".input"));
        new Interpreter(
                reader,
                new PrintWriter(System.out)
        ).interpretCompilationUnit(compilationUnit);
    }

    public void interpretCompilationUnit(AST.CompilationUnit compilationUnit) {
        interpretCompoundStatement(compilationUnit.getCompoundStatement());
    }

    public void interpretCompoundStatement(AST.CompoundStatement compoundStatement) {
        for (AST.SingleStatement singleStatement : compoundStatement.getStatements()) {
            interpretSingleStatement(singleStatement);
        }
    }

    public void interpretSingleStatement(AST.SingleStatement singleStatement) {
        if (singleStatement instanceof AST.ExpressionStatement) {
            interpretExpressionStatement((AST.ExpressionStatement) singleStatement);
        }
        if (singleStatement instanceof AST.IfStatement) {
            interpretIfStatement((AST.IfStatement) singleStatement);
        }
        if (singleStatement instanceof AST.WhileStatement) {
            interpretWhileStatement((AST.WhileStatement) singleStatement);
        }
        if (singleStatement instanceof AST.RepeatStatement) {
            interpretRepeatStatement((AST.RepeatStatement) singleStatement);
        }
        if (singleStatement instanceof AST.ForStatement) {
            interpretForStatement((AST.ForStatement) singleStatement);
        }
    }

    public void interpretIfStatement(AST.IfStatement ifStatement) {
        Object c;
        for (int i = 0; i < ifStatement.getCompoundStatements().size(); ++i) {
            if (ifStatement.getConditions().size() <= i) {
                interpretCompoundStatement(ifStatement.getCompoundStatements().get(i));
                break;
            }
            c = interpretExpression(ifStatement.getConditions().get(i));
            if (!c.equals(0)) {
                interpretCompoundStatement(ifStatement.getCompoundStatements().get(i));
                break;
            }
        }
    }

    public void interpretWhileStatement(AST.WhileStatement whileStatement) {
        Object c = interpretExpression(whileStatement.getCondition());
        if (c.equals(0)) {
            return;
        }
        do {
            interpretCompoundStatement(whileStatement.getCompoundStatement());
            c = interpretExpression(whileStatement.getCondition());
            //log.info("while condition " + c);
        } while (!c.equals(0));
    }

    public void interpretRepeatStatement(AST.RepeatStatement repeatStatement) {
        Object c;
        do {
            interpretCompoundStatement(repeatStatement.getCompoundStatement());
            c = interpretExpression(repeatStatement.getCondition());
        } while (c.equals(0));
    }

    public void interpretForStatement(AST.ForStatement forStatement) {
        interpretExpression(forStatement.getInit());
        Object c = interpretExpression(forStatement.getCondition());
        if (c.equals(0)) {
            return;
        }
        do {
            interpretCompoundStatement(forStatement.getCompoundStatement());
            interpretExpression(forStatement.getLoop());
            c = interpretExpression(forStatement.getCondition());
        } while (!c.equals(0));
    }

    public Object interpretExpressionStatement(AST.ExpressionStatement expressionStatement) {
        return interpretExpression(expressionStatement.getExpression());
    }

    public Object interpretExpression(AST.Expression expression) {
        if (expression instanceof AST.Skip) {
            return 0;
        }
        if (expression instanceof AST.BinaryExpression) {
            AST.BinaryExpression binaryExpression = (AST.BinaryExpression) expression;
            Object lo = interpretExpression(binaryExpression.getLeft());
            Object ro = interpretExpression(binaryExpression.getRight());
            if (lo.getClass() != ro.getClass()) {
                throw new RuntimeException();
            }
            switch (binaryExpression.getOperator()) {
                case "*":
                    return (Integer) lo * (Integer) ro;
                case "/":
                    return (Integer) lo / (Integer) ro;
                case "%":
                    return (Integer) lo % (Integer) ro;
                case "+":
                    return (Integer) lo + (Integer) ro;
                case "-":
                    return (Integer) lo - (Integer) ro;
                case "&":
                    return (Integer) lo & (Integer) ro;
                case "^":
                    return (Integer) lo ^ (Integer) ro;
                case "|":
                    return (Integer) lo | (Integer) ro;
                case "&&":
                    return Objects.equals(lo, 1) && Objects.equals(ro, 1) ? 1 : 0;
                case "||":
                    return Objects.equals(lo, 1) || Objects.equals(ro, 1) ? 1 : 0;
                case ">":
                    return (Integer) lo > (Integer) ro ? 1 : 0;
                case "<":
                    return (Integer) lo < (Integer) ro ? 1 : 0;
                case "<=":
                    return (Integer) lo <= (Integer) ro ? 1 : 0;
                case ">=":
                    return (Integer) lo >= (Integer) ro ? 1 : 0;
                case "==":
                    return Objects.equals(lo, ro) ? 1 : 0;
                case "!=":
                    return !Objects.equals(lo, ro) ? 1 : 0;
            }
            throw new IllegalArgumentException();
        }
        if (expression instanceof AST.FunctionCall) {
            AST.FunctionCall functionCall = (AST.FunctionCall) expression;
            switch (functionCall.getName()) {
                case "read":
                    if (functionCall.getArguments().size() != 0) {
                        throw new RuntimeException();
                    }
                    return reader.nextInt();
                case "write": {
                    if (functionCall.getArguments().size() != 1) {
                        throw new RuntimeException();
                    }
                    Object value = interpretExpression(functionCall.getArguments().get(0));
                    System.out.println(value);
                    return 0;
                }
                case "sleep": {
                    if (functionCall.getArguments().size() != 1) {
                        throw new RuntimeException();
                    }
                    Object value = interpretExpression(functionCall.getArguments().get(0));
                    try {
                        Thread.sleep((Integer) value);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            }
            throw new IllegalArgumentException();
        }
        if (expression instanceof AST.Literal) {
            AST.Literal literal = (AST.Literal) expression;
            return literal.getValue();
        }
        if (expression instanceof AST.MemoryAccessExpression) {
            AST.MemoryAccessExpression memoryAccessExpression = (AST.MemoryAccessExpression) expression;
            return interpretMemoryAccess(memoryAccessExpression);
        }
        if (expression instanceof AST.AssignmentExpression) {
            AST.AssignmentExpression assignmentExpression = (AST.AssignmentExpression) expression;
            return interpretAssignment(assignmentExpression);
        }
        throw new IllegalArgumentException();
    }

    private Object interpretMemoryAccess(AST.MemoryAccessExpression memoryAccessExpression) {
        if (memoryAccessExpression instanceof AST.VariableAccessExpression) {
            AST.VariableAccessExpression variableAccess = (AST.VariableAccessExpression) memoryAccessExpression;
            return memory.get(variableAccess.getName());
        }
        throw new IllegalArgumentException();
    }


    public Object interpretAssignment(AST.AssignmentExpression assignmentExpression) {
        if (assignmentExpression.getMemoryAccess() instanceof AST.VariableAccessExpression) {
            AST.VariableAccessExpression variableAccess = (AST.VariableAccessExpression) assignmentExpression.getMemoryAccess();
            Object value = interpretExpression(assignmentExpression.getExpression());
            //log.info(variableAccess.getName() + ":=" + value);
            memory.put(variableAccess.getName(), value);
            return value;
        }
        throw new IllegalArgumentException();
    }
}
