package red.jackf.chesttracker.api.memory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.api.memory.counting.CountingPredicate;
import red.jackf.chesttracker.api.memory.counting.StackMergeMode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>An interface representing a memory key of a memory bank. This is usually a dimension, but can also be for special
 * containers such as ender chests.</p>
 *
 * <p>(Currently) all memory keys use {@link BlockPos} as their keys; for situations that don't exactly fit it is recommended
 * to have a two-way mapping for your keys (i.e. for a paginated menu, the X coordinate being the page).</p>
 */
public interface MemoryKey {
    /**
     * Whether this memory key contains no memories at all. Shouldn't <i>really</i> ever return true as it should be pruned.
     *
     * @return Whether this memory key contains no memories.
     */
    boolean isEmpty();

    /**
     * Returns an immutable map of all memories contained in this memory key.
     *
     * @return All memories contained in this memory key.
     */
    Map<BlockPos, Memory> getMemories();

    /**
     * <p>Returns an immutable map of all memories contained in this memory key that have a saved name, either user-supplied
     * or custom.</p>
     *
     * <p><b>This does not mean that all memories have a renderable name! It may get filtered by the memory bank settings.</b></p>
     *
     * @return All memories contained in this memory key with custom names.
     */
    Map<BlockPos, Memory> getNamedMemories();

    /**
     * Returns a memory at a given position, or an empty optional if none is present. Considers connected blocks from
     * {@link  Memory#otherPositions()}.
     *
     * @param position Position to look for memories at.
     * @return An optional containing the memory at the given position, or null if none present.
     */
    Optional<Memory> get(BlockPos position);

    /**
     * Returns a list of all memories in this key that match the given predicate.
     *
     * @see CountingPredicate
     * @see StackMergeMode
     * @param predicate Predicate to test each memory against - this tests the in-key block position and memory itself.
     * @param stackMergeMode How to merge stacks that pass the predicate - for more details, see {@link StackMergeMode}.
     * @return A list of ItemStacks that pass the predicate in this memory key, merged according to stackMergeMode.
     */
    List<ItemStack> getCounts(CountingPredicate predicate, StackMergeMode stackMergeMode);
}
