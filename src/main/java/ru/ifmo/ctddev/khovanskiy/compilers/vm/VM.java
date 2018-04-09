package ru.ifmo.ctddev.khovanskiy.compilers.vm;

import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.khovanskiy.compilers.ast.evaluator.NullPointer;

public abstract class VM {
    @Getter
    @ToString
    public static class Comment extends VM {
        private final String text;

        public Comment(final String text) {
            this.text = text;
        }
    }

    @Getter
    @ToString
    public static class Dup extends VM {
    }

    @Getter
    @ToString
    public abstract static class Store extends VM {
    }

    @Getter
    @ToString
    public static class IStore extends Store {
        private final int name;

        public IStore(final int id) {
            this.name = id;
        }
    }

    @Getter
    @ToString
    public static class AStore extends Store {
        private final int name;

        public AStore(final int id) {
            this.name = id;
        }
    }

    @Getter
    @ToString
    public static class IAStore extends Store {
    }

    @Getter
    @ToString
    public static class AAStore extends Store {
    }

    @Getter
    @ToString
    public abstract static class Load extends VM {
    }

    @Getter
    @ToString
    public static class ILoad extends Load {
        private final int name;

        public ILoad(final int id) {
            this.name = id;
        }
    }

    @Getter
    @ToString
    public static class ALoad extends Load {
        private final int name;

        public ALoad(final int id) {
            this.name = id;
        }
    }

    @Getter
    @ToString
    public static class IALoad extends Load {
    }

    @Getter
    @ToString
    public static class AALoad extends Load {
    }

    public static class Push extends VM {

    }

    @Getter
    @ToString
    public static class Pop extends VM {

    }

    @Getter
    @ToString
    public abstract static class Const<T> extends VM {
        private final T value;

        protected Const(final T value) {
            this.value = value;
        }
    }

    @Getter
    @ToString
    public abstract static class AConst<T> extends Const<T> {
        protected AConst(final T value) {
            super(value);
        }
    }

    @Getter
    @ToString
    public static class AConstNull extends AConst<NullPointer> {
        public AConstNull() {
            super(null);
        }
    }

    @Getter
    @ToString
    public static class IConst extends Const<Integer> {
        public IConst(final int value) {
            super(value);
        }
    }

    @Getter
    @ToString
    public static class BinOp extends VM {
        private final String operator;

        public BinOp(final String operator) {
            this.operator = operator;
        }
    }

    @Getter
    @ToString
    public abstract static class AbstractInvoke extends VM {
    }

    @Getter
    @ToString
    public static class InvokeStatic extends AbstractInvoke {
        private final String name;
        private final int argumentsCount;

        public InvokeStatic(final String name, final int argumentsCount) {
            this.name = name;
            this.argumentsCount = argumentsCount;
        }
    }

    @Getter
    @ToString
    public abstract static class AbstractReturn<T> extends VM {
    }

    @Getter
    @ToString
    public static class Return extends AbstractReturn<Void> {
        public Return() {
        }
    }

    @Getter
    @ToString
    public static class IReturn extends AbstractReturn<Integer> {
        public IReturn() {
        }
    }

    @Getter
    @ToString
    public static class AReturn extends AbstractReturn<Object> {

    }

    @Getter
    @ToString
    public static class Label extends VM {
        private final String name;

        public Label(final String name) {
            this.name = name;
        }
    }

    @Getter
    @ToString
    public static class Begin extends VM {
        private final String name;
        private final int argumentsCount;

        public Begin(final String name, final int argumentsCount) {
            this.name = name;
            this.argumentsCount = argumentsCount;
        }
    }

    @Getter
    @ToString
    public static class Goto extends VM {
        private final String label;

        public Goto(final String label) {
            this.label = label;
        }
    }

    @Getter
    @ToString
    public static class IfTrue extends VM {
        private final String label;

        public IfTrue(final String label) {
            this.label = label;
        }
    }

    @Getter
    @ToString
    public static class IfFalse extends VM {
        private final String label;

        public IfFalse(final String label) {
            this.label = label;
        }
    }

    @Getter
    @ToString
    public static class NewArray extends VM {

    }

//    @Getter
//    @ToString
//    public static class Var extends VM {
//        private final String name;
//
//        public Var(String name) {
//            this.name = name;
//        }
//    }
//
//    @Getter
//    @ToString
//    public static class Array extends VM {
//        private final int index;
//
//        public Array(int index) {
//            this.index = index;
//        }
//    }
}
