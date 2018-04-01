package ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler;

import lombok.Getter;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.RenameHolder;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMFunction;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class CompilerContext {
    private VMProgram vmProgram = new VMProgram();
    private AtomicInteger labelIds = new AtomicInteger(0);
    private Stack<Scope> scopes = new Stack<>();

    public CompilerContext() {
        this.scopes.add(new Scope());
    }

    public void addCommand(final VM command) {
        vmProgram.getFunctions().get(vmProgram.getFunctions().size() - 1).getCommands().add(command);
    }

    public void registerFunction(final String name, int size) {
        vmProgram.getFunctions().add(new VMFunction(name, size));
    }

    public String getNextLabel() {
        return "l" + labelIds.getAndIncrement();
    }

    public Scope getScope() {
        return this.scopes.peek();
    }

    public static class Scope {
        private RenameHolder renameHolder = new RenameHolder();

        public int rename(String name) {
            return this.renameHolder.rename(name);
        }
    }
}
