package red.jackf.chesttracker.util;

import java.util.function.Consumer;

public class Misc {
    public static <T> T let(T obj, Consumer<T> op) {
        op.accept(obj);
        return obj;
    }

    public static int lerpDiscrete(float factor, int a, int b) {
        return (int) (a + (b - a) * factor);
    }
}
