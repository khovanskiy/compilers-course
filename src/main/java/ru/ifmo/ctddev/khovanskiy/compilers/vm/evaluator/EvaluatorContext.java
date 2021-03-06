package ru.ifmo.ctddev.khovanskiy.compilers.vm.evaluator;

import lombok.Getter;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.Pointer;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.Symbol;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.RenameHolder;

import java.util.*;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class EvaluatorContext {
    private final Map<Pointer, Symbol> externals;
    private final List<Integer> heap = new ArrayList<>();
    private final Map<String, Position> labels = new HashMap<>();
    private final Stack<Symbol> stack = new Stack<>();
    private final Stack<Position> callStack = new Stack<>();
    private Position position;
    private final Stack<Scope> scopes = new Stack<>();

    public EvaluatorContext(final Map<Pointer, Symbol> externals) {
        this.externals = externals;
        this.scopes.push(new Scope());
    }

    public Scope getScope() {
        return this.scopes.peek();
    }

    public void put(Pointer pointer, Symbol<?> symbol) {
        if (externals.containsKey(pointer)) {
            throw new IllegalStateException();
        }
        final Scope scope = getScope();
        scope.getData().put(pointer, symbol);
    }

    @SuppressWarnings("unchecked")
    public <T> Symbol<T> get(final Pointer pointer, final Class<T> clazz) {
        final Symbol external = externals.get(pointer);
        if (external != null) {
            return external;
        }
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

    public void setLabels(final Map<String, Position> labels) {
        this.labels.clear();
        this.labels.putAll(labels);
    }

    public void gotoLabel(String name) {
        Position position = labels.get(name);
        if (position == null) {
            throw new IllegalStateException(String.format("Unknown position of label \"%s\"", name));
        }
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(final Position position) {
        this.position = position;
    }

    public Position nextPosition() {
        this.position = new Position(this.position.getFunctionName(), this.position.getLineNumber() + 1);
        return position;
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

        public int rename(String name) {
            return this.renameHolder.rename(name);
        }

        public int nextName() {
            return this.renameHolder.nextName();
        }
    }
}
