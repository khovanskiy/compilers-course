package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import lombok.Getter;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;

@Getter
public class UserFunction extends MyFunction {
    private final AST.FunctionDefinition definition;

    public UserFunction(final AST.FunctionDefinition definition) {
        this.definition = definition;
    }
}
