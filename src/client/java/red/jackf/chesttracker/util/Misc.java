package red.jackf.chesttracker.util;

import java.util.function.Consumer;

public class Misc {
    public static <T> T let(T obj, Consumer<T> op) {
        op.accept(obj);
        return obj;
    }
}
