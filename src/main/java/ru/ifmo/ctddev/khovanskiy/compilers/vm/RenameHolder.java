package ru.ifmo.ctddev.khovanskiy.compilers.vm;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class RenameHolder {
    private Map<String, String> remapping = new HashMap<>();
    private AtomicInteger variableIds = new AtomicInteger(0);

    public String rename(String name) {
        String newName = remapping.get(name);
        if (newName == null) {
            newName = nextName();
            remapping.put(name, newName);
        }
        return newName;
    }

    public String nextName() {
        return "v" + variableIds.getAndIncrement();
    }
}
