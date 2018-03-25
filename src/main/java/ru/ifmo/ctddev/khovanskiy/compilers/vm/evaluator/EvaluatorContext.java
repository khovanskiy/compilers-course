package ru.ifmo.ctddev.khovanskiy.compilers.vm.evaluator;

import lombok.Getter;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.ExternalFunction;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.Pointer;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.Symbol;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.RenameHolder;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class EvaluatorContext {
    private final Map<String, ExternalFunction> externalFunctions;
    private final List<Integer> heap = new ArrayList<>();
    private final Map<String, Integer> labels = new HashMap<>();
    private final Stack<Symbol> stack = new Stack<>();
    private final Stack<Integer> callStack = new Stack<>();
    private final AtomicInteger pointer = new AtomicInteger(0);
    private final Stack<Scope> scopes = new Stack<>();

    public EvaluatorContext(final Map<String, ExternalFunction> externalFunctions) {
        this.externalFunctions = externalFunctions;
        this.scopes.push(new Scope());
    }

    public Scope getScope() {
        return this.scopes.peek();
    }

    public void put(Pointer pointer, Symbol<?> symbol) {
        final Scope scope = getScope();
        scope.getData().put(pointer, symbol);
    }

    @SuppressWarnings("unchecked")
    public <T> Symbol<T> get(final Pointer pointer, final Class<T> clazz) {
        for (int i = scopes.size() - 1; i >= 0; --i) {
            final Scope scope = scopes.get(i);
            final Symbol symbol = scope.getData().get(pointer);
            if (symbol != null) {
                if (!clazz.isInstance(symbol.getValue())) {
                    throw new IllegalStateException();
                }
                return symbol;
            }
        }
        return null;
    }

    public void setLabels(final Map<String, Integer> labels) {
        this.labels.clear();
        this.labels.putAll(labels);
    }

    public void gotoLabel(String name) {
        Integer position = labels.get(name);
        if (position == null) {
            throw new IllegalStateException(String.format("Unknown position of label \"%s\"", name));
        }
        pointer.set(position);
    }

    public int getPosition() {
        return pointer.get();
    }

    public void setPosition(int position) {
        pointer.set(position);
    }

    public int nextPosition() {
        return pointer.incrementAndGet();
    }

    @Getter
    public static class Scope {
        private final RenameHolder renameHolder = new RenameHolder();
        private final Map<Pointer, Symbol> data;

        public Scope() {
            this.data = new HashMap<>();
        }

        public Scope(Map<Pointer, Symbol> data) {
            this.data = data;
        }

        public String rename(String name) {
            return this.renameHolder.rename(name);
        }

        public String nextName() {
            return this.renameHolder.nextName();
        }
    }
}
