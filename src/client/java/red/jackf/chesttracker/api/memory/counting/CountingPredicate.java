package red.jackf.chesttracker.api.memory.counting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.api.memory.Memory;
import red.jackf.chesttracker.api.memory.MemoryKey;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Predicate for testing memories based on their position and contents. Used for {@link MemoryKey#getCounts(CountingPredicate, StackMergeMode)}
 */
public interface CountingPredicate extends BiPredicate<BlockPos, Memory>, Predicate<Map.Entry<BlockPos, Memory>> {
    /**
     * Test whether this position-memory pair should be accepted.
     *
     * @param position Position of the memory.
     * @param memory   the second input argument
     * @return Whether this position-memory pair passes the predicate.
     */
    boolean test(BlockPos position, Memory memory);

    /**
     * Helper predicate that always passes.
     */
    CountingPredicate TRUE = (pos, memory) -> true;

    /**
     * Predicate that accepts memories within a sphere around a point.
     *
     * @param origin Origin point to keep within.
     * @param radius Radius around the point to accept memories within.
     * @return Predicate accepting memories around a radius
     */
    static CountingPredicate within(Vec3 origin, double radius) {
        final double sqRadius = radius * radius;
        return (pos, memory) -> pos.distToCenterSqr(origin) < sqRadius;
    }

    // Methods for keeping type when composing predicates

    @NotNull
    default CountingPredicate and(@NotNull CountingPredicate other) {
        return (pos, memory) -> this.test(pos, memory) && other.test(pos, memory);
    }

    @NotNull
    default CountingPredicate or(@NotNull CountingPredicate other) {
        return (pos, memory) -> this.test(pos, memory) || other.test(pos, memory);
    }

    @NotNull
    default CountingPredicate negate() {
        return (pos, memory) -> !this.test(pos, memory);
    }

    @ApiStatus.Internal
    default boolean test(Map.Entry<BlockPos, Memory> mapEntry) {
        return this.test(mapEntry.getKey(), mapEntry.getValue());
    }
}
