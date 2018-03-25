package ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler;

import lombok.Getter;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.RenameHolder;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
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

    public void addCommand(VM command) {
        vmProgram.getCommands().add(command);
    }

    public String getNextLabel() {
        return "l" + labelIds.getAndIncrement();
    }

    public Scope getScope() {
        return this.scopes.peek();
    }

    public static class Scope {
        private RenameHolder renameHolder = new RenameHolder();

        public String rename(String name) {
            if ("true".equals(name) || "false".equals(name)) {
                return name;
            }
            return this.renameHolder.rename(name);
        }
    }
}
