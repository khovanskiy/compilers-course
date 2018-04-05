package ru.ifmo.ctddev.khovanskiy.compilers.vm;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class RenameHolder {
    private final Map<String, Integer> namesToIdsMap = new HashMap<>();

    private final Map<Integer, String> idsToNamesMap = new HashMap<>();

    private final AtomicInteger variableIds = new AtomicInteger(0);

    public int rename(final String name) {
        Integer id = namesToIdsMap.get(name);
        if (id == null) {
            id = nextName();
            namesToIdsMap.put(name, id);
            idsToNamesMap.put(id, name);
        }
        return id;
    }

    public String getNameById(int id) {
        return idsToNamesMap.get(id);
    }

    public int nextName() {
        return variableIds.getAndIncrement();
    }
}
