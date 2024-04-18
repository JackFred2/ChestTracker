package red.jackf.chesttracker.impl.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Misc {
    /**
     * Takes an object and returns it after running an operation on it. Used for field initialization.
     * @param obj Object to run a method on and return
     * @param op Method to run on said object
     * @return obj
     * @param <T> Type of obj
     */
    public static <T> T let(T obj, Consumer<T> op) {
        op.accept(obj);
        return obj;
    }

    /**
     * Returns a sorter that brings elements in a given "head" list to the front, in the given order.
     * @param head List to check elements for and bring forwards
     * @return Comparator bringing specified elements to the front
     * @param <T> Type of elements being sorted
     */
    public static <T> Comparator<T> bringToFront(List<T> head) {
        final var immutable = List.copyOf(head);
        return (a, b) -> {
                int aIndex = immutable.indexOf(a);
                int bIndex = immutable.indexOf(b);
                if (aIndex != -1) {
                    if (bIndex != -1) {
                        return aIndex - bIndex;
                    } else {
                        return -1;
                    }
                } else {
                    if (bIndex != -1) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
        };
    }

    /**
     * Runs a function, returning both it's result and how long it took in nanoseconds to complete.
     * @param func Function to time
     * @return Pair of the function's result and time taken
     * @param <T> Return value type
     */
    public static <T> Pair<T, Long> time(Supplier<T> func) {
        var before = System.nanoTime();
        var result = func.get();
        return Pair.of(result, System.nanoTime() - before);
    }

    /**
     * Cycle an enum value in order of declaration
     * @param current Current value to cycle from
     * @return Next enum value, in declaration order
     * @param <E> Enum class
     */
    public static <E extends Enum<E>> E next(E current) {
        E[] options = current.getDeclaringClass().getEnumConstants();
        if (options.length <= 1) return current;
        int targetOrdinal = (current.ordinal() + 1) % options.length;
        return options[targetOrdinal];
    }

    /**
     * <p>Given a origin point and list of other positions, calculate the average position as an offset from origin. E.g.:</p>
     * <ul>
     *     <li>origin (2,4,5) ; others [] = (0,0,0)</li>
     *     <li>origin (2,4,5) ; others [(2,4,9)] = (0,0,2)</li>
     * </ul>
     *
     * @param origin Center point to base around.
     * @param others Other positions around the origin.
     * @return Average position as an offset from origin.
     */
    public static Vec3 getAverageOffsetFrom(BlockPos origin, Collection<BlockPos> others) {
        var accum = new Vec3(0, 0, 0);
        if (others.isEmpty()) return accum;
        var scale = 1f / (1 + others.size());
        for (BlockPos other : others)
            accum = accum.add(Vec3.atLowerCornerOf(other.subtract(origin)).scale(scale));
        return accum;
    }
}
