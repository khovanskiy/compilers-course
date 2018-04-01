package ru.ifmo.ctddev.khovanskiy.compilers.vm;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class VMProgram {
    private final List<VMFunction> functions;

    public VMProgram() {
        this.functions = new ArrayList<>();
    }

    public VMProgram(List<VMFunction> functions) {
        this.functions = functions;
    }
}
