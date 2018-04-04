package ru.ifmo.ctddev.khovanskiy.compilers.vm.inference.type;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
public class CharacterType extends ObjectType {
    public static final CharacterType INSTANCE = new CharacterType();

    protected CharacterType() {
        super();
    }

    @Override
    public ObjectType getParentType() {
        return ObjectType.INSTANCE;
    }

    @Override
    public String toString() {
        return "<Char>";
    }
}
