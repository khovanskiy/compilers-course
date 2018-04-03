package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference;

import java.util.HashSet;
import java.util.Set;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class SetUtils {
    public static <T> Set<T> union(Set<T> setA, Set<T> setB) {
        return mutableUnion(new HashSet<>(setA), setB);
    }

    public static <T> Set<T> mutableUnion(Set<T> setA, Set<T> setB) {
        setA.addAll(setB);
        return setA;
    }

    public static <T> Set<T> intersection(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new HashSet<>();
        for (T x : setA) {
            if (setB.contains(x)) {
                tmp.add(x);
            }
        }
        return tmp;
    }
}
