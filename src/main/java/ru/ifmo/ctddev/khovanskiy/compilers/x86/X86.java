package ru.ifmo.ctddev.khovanskiy.compilers.x86;

import lombok.Getter;
import lombok.ToString;
import ru.ifmo.ctddev.khovanskiy.compilers.x86.register.Register;

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
    public static class XorL extends X86 {
        private final Register source;
        private final Register destination;

        public XorL(Register source, Register destination) {
            this.source = source;
            this.destination = destination;
        }
    }
}
