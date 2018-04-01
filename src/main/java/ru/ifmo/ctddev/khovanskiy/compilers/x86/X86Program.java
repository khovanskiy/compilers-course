package ru.ifmo.ctddev.khovanskiy.compilers.x86;

import lombok.Getter;

import java.util.List;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class X86Program {
    private final List<X86> commands;

    public X86Program(List<X86> commands) {
        this.commands = commands;
    }
}
