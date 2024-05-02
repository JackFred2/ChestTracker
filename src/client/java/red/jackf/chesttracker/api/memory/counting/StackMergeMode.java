package red.jackf.chesttracker.api.memory.counting;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

/**
 * <p>Stack merging method for counting methods in {@link red.jackf.chesttracker.api.memory.MemoryKey#getCounts(CountingPredicate, StackMergeMode)}.</p>
 *
 * <p>Item stacks are able to be merged if they share the same item ID and NBT.</p>
 *
 * <p>For the examples below, imagine there are 3 memories with the given item stacks:</p>
 *
 * <ol>
 *     <li>[16 redstone, 16 redstone]</li>
 *     <li>[64 redstone, 64 iron_ingot]</li>
 *     <li>[16 iron_ingot]</li>
 * </ol>
 */
public enum StackMergeMode {
    /**
     * <p>Merge all stacks, regardless of container.</p>
     *
     * <p>Example result: [96 redstone, 80 iron_ingot]</p>
     */
    ALL(Component.translatable("chesttracker.gui.editMemoryBank.search.stackMergeMode.all")),

    /**
     * <p>Merge all stacks within each memory, but not between memory.</p>
     *
     * <p>Example result: [32 redstone, 32 redstone, 64 iron_ingot, 16 iron_ingot]</p>
     */
    WITHIN_CONTAINERS(Component.translatable("chesttracker.gui.editMemoryBank.search.stackMergeMode.withinContainers")),

    /**
     * <p>Never merge stacks, even within each memory.</p>
     *
     * <p>Example result: [16 redstone, 16 redstone, 64 redstone, 64 iron_ingot, 16 iron_ingot]</p>
     */
    NEVER(Component.translatable("chesttracker.gui.editMemoryBank.search.stackMergeMode.never"));

    /**
     * Label used in the settings screen.
     */
    @ApiStatus.Internal
    public final Component label;

    StackMergeMode(Component label) {
        this.label = label;
    }
}
