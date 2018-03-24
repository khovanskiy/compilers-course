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
    private final List<VM> commands;

    public VMProgram() {
        this.commands = new ArrayList<>();
    }

    public VMProgram(List<VM> commands) {
        this.commands = commands;
    }
}
