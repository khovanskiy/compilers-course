package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type;

import lombok.Getter;

import java.util.Collections;
import java.util.Set;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class TypeVariable extends Type {
    private final int id;

    private String functionName;

    private int variableId;

    private String variableName;

    private boolean ignore;

    public TypeVariable(final int id) {
        this.id = id;
    }

    public TypeVariable(final int id, final String functionName, final int variableId, final String variableName, final boolean ignore) {
        this.id = id;
        this.functionName = functionName;
        this.variableId = variableId;
        this.variableName = variableName;
        this.ignore = ignore;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof TypeVariable && this.id == ((TypeVariable) obj).id;
    }

    @Override
    public String toString() {
        if (functionName != null) {
            return functionName + "." + variableName;
        }
        return "T" + id;
    }

    @Override
    public boolean isConcreteType() {
        return false;
    }

    @Override
    public Type substitute(final TypeVariable typeVariable, final Type substitution) {
        if (this.equals(typeVariable)) {
            return substitution;
        }
        return this;
    }

    @Override
    public Set<TypeVariable> getTypeVariables() {
        return Collections.singleton(this);
    }
}
