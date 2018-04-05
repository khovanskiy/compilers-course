package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference;

import lombok.Getter;
import lombok.Setter;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.ConcreteType;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class TypeContext {
    private final Map<String, Scope> scopes = new HashMap<>();

    public Scope getScopeByName(final String name) {
        return scopes.computeIfAbsent(name, k -> new Scope(name));
    }

    @Getter
    @Setter
    public class Scope {
        private final Map<Integer, ConcreteType> variableTypes = new HashMap<>();

        private final String name;

        private ConcreteType returnType;

        public Scope(final String name) {
            this.name = name;
        }

        public ConcreteType getVariableType(final int id) {
            return variableTypes.computeIfAbsent(id, k -> {
                throw new IllegalStateException();
            });
        }

        public void setVariableType(final int id, final ConcreteType type) {
            variableTypes.put(id, type);
        }
    }
}
