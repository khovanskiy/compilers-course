package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.RenameHolder;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.Type;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type.TypeVariable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
@Slf4j
class TypeInferenceContext {
    private final Map<String, Scope> functionScopes = new HashMap<>();

    private final AtomicInteger typeVariableIds = new AtomicInteger(0);

    private final List<TypeRelation> relations = new ArrayList<>();

    private final Stack<Scope> scopes = new Stack<>();

    private TypeContext typeContext;

    public void addTypeRelation(final Type left, final Type right) {
        relations.add(new TypeRelation(left, right));
    }

    public TypeInferenceContext.Scope getScope() {
        return this.scopes.peek();
    }

    public void wrapFunction(final String name, final ExceptionConsumer<Scope> consumer) throws Exception {
        final Scope scope = getScopeByName(name);
        scopes.push(scope);
        consumer.accept(scope);
        scopes.pop();
    }

    public Scope getScopeByName(final String name) {
        return functionScopes.computeIfAbsent(name, k -> new Scope(name));
    }

    public void setTypeContext(final TypeContext typeContext) {
        this.typeContext = typeContext;
    }

    @Getter
    public class Scope {
        private final Map<Integer, TypeVariable> variables = new HashMap<>();

        private final Stack<Type> typeStack = new Stack<>();

        private final RenameHolder renameHolder = new RenameHolder();

        private final String name;

        private TypeVariable returnType;

        Scope(final String name) {
            this.name = name;
        }

        public void pushType(final Type type) {
            typeStack.push(type);
        }

        public Type popType() {
            return typeStack.pop();
        }

        public int getVariableId(final String name) {
            return renameHolder.rename(name);
        }

        public TypeVariable createTypeVariable() {
            return new TypeVariable(typeVariableIds.getAndIncrement());
        }

        public TypeVariable getVariableType(final int id) {
            return getVariableType(id, false);
        }

        public TypeVariable getReturnType() {
            if (returnType == null) {
                returnType = new TypeVariable(typeVariableIds.getAndIncrement(), name, -1, "return", false);
                if (log.isTraceEnabled()) {
                    log.trace("Function \"" + name + "\" has return type variable: " + returnType);
                }
            }
            return returnType;
        }

        public void setReturnType(final Type returnType) {
            this.returnType = getReturnType();
            addTypeRelation(returnType, this.returnType);
        }

        public void setVariableType(final int id, final Type type) {
            setVariableType(id, type, false);
        }

        public void setVariableType(final int id, final Type type, final boolean ignore) {
            final TypeVariable typeVariable = getVariableType(id, ignore);
            addTypeRelation(type, typeVariable);
        }

        private TypeVariable getVariableType(final int id, final boolean ignore) {
            return variables.computeIfAbsent(id, k -> {
                String variableName = renameHolder.getNameById(id);
                if (variableName == null) {
                    variableName = "v" + id;
                }
                final TypeVariable typeVariable = new TypeVariable(typeVariableIds.getAndIncrement(), name, id, variableName, ignore);
                if (log.isTraceEnabled()) {
                    log.trace("Function \"" + name + "\" Variable v" + id + " has type variable: " + typeVariable);
                }
                return typeVariable;
            });
        }
    }
}
