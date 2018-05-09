package ru.ifmo.ctddev.khovanskiy.compilers.vm.compiler;

import lombok.Getter;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.RenameHolder;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VM;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMFunction;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.VMProgram;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.TypeContext;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.ConcreteType;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * The virtual machine compiler context
 *
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class VMCompilerContext {
    private final TypeContext typeContext;

    private final VMProgram vmProgram = new VMProgram();

    private final AtomicInteger labelIds = new AtomicInteger(0);

    private final Stack<Scope> scopes = new Stack<>();

    private final Stack<Loop> loops = new Stack<>();

    public VMCompilerContext(final TypeContext typeContext) {
        this.typeContext = typeContext;
    }

    /**
     * Wraps the function body in new scope and compiles function in it
     *
     * @param name     the function name
     * @param consumer the scope consumer
     * @return the new scope
     * @since 1.0.0
     */
    public Scope wrapFunction(final String name, final Consumer<Scope> consumer) {
        Scope scope = new Scope(name);
        scopes.push(scope);
        consumer.accept(scope);
        scope = scopes.pop();
        return scope;
    }

    /**
     * @param startLabel the label of the loop start
     * @param endLabel   the label of the loop end
     * @param consumer   the loop consumer
     * @since 1.0.0
     */
    public void wrapLoop(final String startLabel, final String endLabel, final Consumer<Loop> consumer) {
        final Loop loop = new Loop(startLabel, endLabel);
        loops.push(loop);
        consumer.accept(loop);
        loops.pop();
    }

    public void addCommand(final VM command) {
        vmProgram.getFunctions().get(vmProgram.getFunctions().size() - 1).getCommands().add(command);
    }

    public void registerFunction(final String name, final int argumentsCount, List<ConcreteType> types, ConcreteType returnType) {
        vmProgram.getFunctions().add(new VMFunction(name, argumentsCount, types, returnType));
    }

    public String getNextLabel() {
        return "l" + labelIds.getAndIncrement();
    }

    public Scope getScope() {
        return this.scopes.peek();
    }

    public Loop getLoop() {
        return this.loops.peek();
    }

    @Getter
    public static class Scope {
        private final RenameHolder renameHolder = new RenameHolder();

        private final String name;

        public Scope(final String name) {
            this.name = name;
        }

        public int rename(final String name) {
            return this.renameHolder.rename(name);
        }
    }

    @Getter
    public static class Loop {
        private final String loopLabel;
        private final String endLabel;

        public Loop(String loopLabel, String endLabel) {
            this.loopLabel = loopLabel;
            this.endLabel = endLabel;
        }
    }
}
