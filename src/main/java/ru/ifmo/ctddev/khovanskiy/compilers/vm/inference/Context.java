package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference;

import lombok.Getter;
import ru.ifmo.ctddev.khovanskiy.compilers.vm.RenameHolder;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Getter
public class Context {
    private final Map<String, Scope> functionScopes = new HashMap<>();

//    private final Map<TypeVariable, Set<Type>> upperBounds = new HashMap<>();
//
//    private final Map<TypeVariable, Set<Type>> lowerBounds = new HashMap<>();

    private final AtomicInteger typeVariableIds = new AtomicInteger(0);

    private final List<TypeRelation> relations = new ArrayList<>();

    private final Stack<Scope> scopes = new Stack<>();

    public void addTypeRelation(final Type left, final Type right) {
        relations.add(new TypeRelation(left, right));
//        if (left instanceof TypeVariable) {
//            getLowerBounds((TypeVariable) left).add(right);
//        }
//        if (right instanceof TypeVariable) {
//            getUpperBounds((TypeVariable) right).add(left);
//        }
    }

//    public Set<Type> getLowerBounds(final TypeVariable typeVariable) {
//        return lowerBounds.computeIfAbsent(typeVariable, k -> new HashSet<>());
//    }
//
//    public Set<Type> getUpperBounds(final TypeVariable typeVariable) {
//        return upperBounds.computeIfAbsent(typeVariable, k -> new HashSet<>());
//    }

    public Context.Scope getScope() {
        return this.scopes.peek();
    }

    public void wrapFunction(final String name, final ExceptionConsumer<Scope> consumer) throws Exception {
        final Scope scope = getScopeByName(name);
        scopes.push(scope);
        consumer.accept(scope);
        scopes.pop();
    }

    public Scope getScopeByName(final String name) {
        return functionScopes.computeIfAbsent(name, k -> new Scope());
    }

    @Getter
    public class Scope {
        private final Map<Integer, TypeVariable> variables = new HashMap<>();

        private final Stack<Type> typeStack = new Stack<>();

        private final RenameHolder renameHolder = new RenameHolder();

        private TypeVariable returnType;

        public void pushType(final Type type) {
            typeStack.push(type);
        }

        public Type popType() {
            return typeStack.pop();
        }

        public Type peekType() {
            return typeStack.peek();
        }

        public int getVariableId(final String name) {
            return renameHolder.rename(name);
        }

        public TypeVariable getVariableType(final int id) {
            return variables.computeIfAbsent(id, k -> new TypeVariable(typeVariableIds.getAndIncrement()));
        }

        public Type getReturnType() {
            if (returnType == null) {
                returnType = new TypeVariable(typeVariableIds.getAndIncrement());
            }
            return returnType;
        }

        public void setReturnType(final Type returnType) {
            throw new UnsupportedOperationException();
        }

        public void setVariableType(final int id, Type type) {
            final TypeVariable typeVariable = getVariableType(id);
            addTypeRelation(type, typeVariable);
        }
    }
}
