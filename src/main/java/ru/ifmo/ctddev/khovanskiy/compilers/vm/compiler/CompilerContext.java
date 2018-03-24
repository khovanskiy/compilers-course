package ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler;

import lombok.Getter;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class CompilerContext {
    private VMProgram vmProgram = new VMProgram();
    private AtomicInteger labelIds = new AtomicInteger(0);

    public void addCommand(VM command) {
        vmProgram.getCommands().add(command);
    }

    public String getNextLabel() {
        return "l" + labelIds.getAndIncrement();
    }
}
