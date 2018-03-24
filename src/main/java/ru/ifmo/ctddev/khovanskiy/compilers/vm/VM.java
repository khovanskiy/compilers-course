package ru.ifmo.ctddev.khovanskiy.compilers.vm;

import lombok.Getter;
import lombok.ToString;

public abstract class VM {
    @Getter
    @ToString
    public static class Store extends VM {
        private final String name;

        public Store(String name) {
            this.name = name;
        }
    }

    @Getter
    @ToString
    public static class Load extends VM {
        private final String name;

        public Load(String name) {
            this.name = name;
        }
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

        protected Const(T value) {
            this.value = value;
        }
    }

    @Getter
    @ToString
    public static class IConst extends Const<Integer> {
        public IConst(int value) {
            super(value);
        }
    }

    @Getter
    @ToString
    public static class BinOp extends VM {
        private final String operator;

        public BinOp(String operator) {
            this.operator = operator;
        }
    }

    @Getter
    @ToString
    public abstract static class AbstractInvoke extends VM {
    }

    @Getter
    @ToString
    public static class InvokeExternal extends AbstractInvoke {
        private final String name;
        private final int argumentsCount;

        public InvokeExternal(String name, int argumentsCount) {
            this.name = name;
            this.argumentsCount = argumentsCount;
        }
    }

    @Getter
    @ToString
    public static class InvokeStatic extends AbstractInvoke {
        private final String name;

        public InvokeStatic(String name) {
            this.name = name;
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
    public static class Label extends VM {
        private final String name;

        public Label(String name) {
            this.name = name;
        }
    }

    @Getter
    @ToString
    public static class Goto extends VM {
        private final String label;

        public Goto(String label) {
            this.label = label;
        }
    }

    @Getter
    @ToString
    public static class IfTrue extends VM {
        private final String label;

        public IfTrue(String label) {
            this.label = label;
        }
    }

    @Getter
    @ToString
    public static class IfFalse extends VM {
        private final String label;

        public IfFalse(String label) {
            this.label = label;
        }
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
