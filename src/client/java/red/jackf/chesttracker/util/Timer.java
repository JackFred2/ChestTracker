package red.jackf.chesttracker.util;

import com.mojang.datafixers.util.Pair;

import java.util.function.Supplier;

public class Timer {
    public static <T> Pair<T, Long> time(Supplier<T> func) {
        var before = System.nanoTime();
        var result = func.get();
        return Pair.of(result, System.nanoTime() - before);
    }
}
