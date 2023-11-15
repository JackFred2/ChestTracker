package red.jackf.chesttracker.util;

public class Enums {
    public static <E extends Enum<E>> E next(E current) {
        E[] options = current.getDeclaringClass().getEnumConstants();
        int targetOrdinal = (current.ordinal() + 1) % options.length;
        return options[targetOrdinal];
    }
}
