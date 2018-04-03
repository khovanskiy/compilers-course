package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@FunctionalInterface
public interface ExceptionConsumer<T> {
    void accept(T t) throws Exception;
}
