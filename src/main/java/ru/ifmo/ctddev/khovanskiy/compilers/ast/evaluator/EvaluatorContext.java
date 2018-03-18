package ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EvaluatorContext {
    private final Map<Pointer, Symbol> data;
    private EvaluatorContext previous;
    private Symbol result;
    private boolean shouldReturn;

    public EvaluatorContext(final Map<Pointer, Symbol> symbols) {
        this.data = symbols;
    }

    public EvaluatorContext(EvaluatorContext previous) {
        this.previous = previous;
        this.data = new HashMap<>();
    }

    public void put(Pointer key, Symbol<?> o) {
        this.data.put(key, o);
    }

    public void update(Pointer pointer, Function<Symbol, Symbol> updater) {
        final Symbol oldSymbol = data.get(pointer);
        final Symbol newSymbol = updater.apply(oldSymbol);
        data.put(pointer, newSymbol);
    }

    public <T> Symbol<T> get(Pointer pointer, Class<T> clazz) {
        EvaluatorContext current = this;
        while (current != null) {
            Symbol o = current.data.get(pointer);
            if (o != null) {
                if (!clazz.isInstance(o.getValue())) {
                    throw new IllegalStateException();
                }
                return o;
            }
            current = current.previous;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> Symbol<T> getResult(Class<T> clazz) {
        if (!clazz.isInstance(result.getValue())) {
            throw new IllegalStateException();
        }
        return (Symbol<T>) result;
    }

    public void setResult(Symbol result) {
        this.result = result;
    }

    public boolean isShouldReturn() {
        return shouldReturn;
    }

    public void setShouldReturn(boolean shouldReturn) {
        this.shouldReturn = shouldReturn;
    }
}
