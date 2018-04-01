package ru.ifmo.ctddev.khovanskiy.compilers.x86;

import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Register;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Register8;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public abstract class X86 {
    @Getter
    @ToString
    public static class PushL extends X86 {
        private final MemoryAccess source;

        public PushL(MemoryAccess source) {
            this.source = source;
        }
    }

    @Getter
    @ToString
    public static class PopL extends X86 {
        private final Register destination;

        public PopL(Register destination) {
            this.destination = destination;
        }
    }

    @Getter
    @ToString
    public static class MovL extends X86 {
        private final MemoryAccess source;
        private final MemoryAccess destination;

        public MovL(MemoryAccess source, MemoryAccess destination) {
            this.source = source;
            this.destination = destination;
        }
    }

    @Getter
    @ToString
    public static class Label extends X86 {
        private final String name;

        public Label(String name) {
            this.name = name;
        }
    }

    @Getter
    @ToString
    public static class Call extends X86 {
        private final String label;

        public Call(String label) {
            this.label = label;
        }
    }

    @Getter
    @ToString
    public static class Ret extends X86 {

    }

    @Getter
    @ToString
    public static class AddL extends X86 {
        private final MemoryAccess source;
        private final Register destination;

        public AddL(MemoryAccess source, Register destination) {
            this.source = source;
            this.destination = destination;
        }
    }

    @Getter
    @ToString
    public static class SubL extends X86 {
        private final MemoryAccess source;
        private final Register destination;

        public SubL(MemoryAccess source, Register destination) {
            this.source = source;
            this.destination = destination;
        }
    }

    @Getter
    public static class ImulL extends X86 {
        private final MemoryAccess source;
        private final Register destination;

        public ImulL(MemoryAccess source, Register destination) {
            this.source = source;
            this.destination = destination;
        }
    }

    @Getter
    public static class IDivL extends X86 {
        private final MemoryAccess divider;

        public IDivL(MemoryAccess divider) {
            this.divider = divider;
        }
    }

    @Getter
    public static class Cltd extends X86 {
    }

    @Getter
    public static class Cmp extends X86 {
        private final MemoryAccess left;
        private final MemoryAccess right;

        public Cmp(MemoryAccess left, MemoryAccess right) {
            this.left = left;
            this.right = right;
        }
    }

    @Getter
    @ToString
    public abstract static class Logical extends X86 {
        private final Register left;
        private final Register right;

        public Logical(Register left, Register right) {
            this.left = left;
            this.right = right;
        }
    }

    @Getter
    public static class AndL extends Logical {
        public AndL(Register left, Register right) {
            super(left, right);
        }
    }

    @Getter
    public static class OrL extends Logical {
        public OrL(Register left, Register right) {
            super(left, right);
        }
    }

    @Getter
    public static class XorL extends Logical {
        private final Register source;
        private final Register destination;

        public XorL(Register source, Register destination) {
            super(source, destination);
            this.source = source;
            this.destination = destination;
        }
    }

    @Getter
    @ToString
    public abstract static class Set extends X86 {
        private final Register8 register;

        protected Set(final Register8 register) {
            this.register = register;
        }
    }

    @Getter
    @ToString
    public static class SetG extends Set {
        public SetG(final Register8 register) {
            super(register);
        }
    }

    @Getter
    @ToString
    public static class SetGe extends Set {
        public SetGe(final Register8 register) {
            super(register);
        }
    }

    @Getter
    @ToString
    public static class SetL extends Set {
        public SetL(final Register8 register) {
            super(register);
        }
    }

    @Getter
    @ToString
    public static class SetLe extends Set {
        public SetLe(final Register8 register) {
            super(register);
        }
    }

    @Getter
    @ToString
    public static class SetE extends Set {
        public SetE(final Register8 register) {
            super(register);
        }
    }

    @Getter
    @ToString
    public static class SetNe extends Set {
        public SetNe(final Register8 register) {
            super(register);
        }
    }

    @Getter
    @ToString
    public static class SetNz extends Set {
        public SetNz(final Register8 register) {
            super(register);
        }
    }
}
