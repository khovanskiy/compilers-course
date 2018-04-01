package ru.ifmo.ctddev.khovanskiy.compilers.vm;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class RenameHolder {
    private Map<String, Integer> remapping = new HashMap<>();
    private AtomicInteger variableIds = new AtomicInteger(0);

    public int rename(String name) {
        Integer id = remapping.get(name);
        if (id == null) {
            id = nextName();
            remapping.put(name, id);
        }
        return id;
    }

    public int nextName() {
        return variableIds.getAndIncrement();
    }
}
