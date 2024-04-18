package red.jackf.chesttracker.api.providers;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.memory.Memory;
import red.jackf.chesttracker.api.providers.context.ScreenCloseContext;
import red.jackf.chesttracker.api.providers.defaults.DefaultProviderScreenClose;
import red.jackf.chesttracker.impl.providers.MemoryBuilderImpl;

import java.util.List;

/**
 * A builder for creating memories to submit to a memory bank.
 */
public interface MemoryBuilder {
    /**
     * Create a new Memory Builder with a given list of items to save.
     *
     * @param items Items to save with the Memory
     * @return A new MemoryBuilder, containing the given items.
     */
    static MemoryBuilder create(List<ItemStack> items) {
        return new MemoryBuilderImpl(items.stream().filter(stack -> !stack.isEmpty()).toList());
    }

    /**
     * Adds a custom name to the Memory. If non-null, the player is in the same key as this Memory, and within range,
     * the name will be displayed over the given position.
     *
     * @param name Name to display over the Memory's position.
     * @return This MemoryBuilder.
     */
    MemoryBuilder withCustomName(@Nullable Component name);

    /**
     * Designates that this memory was made in the given container block.
     *
     * @param container Block that this memory is located in
     * @return This MemoryBuilder
     */
    MemoryBuilder inContainer(Block container);

    /**
     * Adds a set of other positions to be highlighted when the Memory is highlighted (think double chests).
     *
     * @param otherPositions Other positions to be highlighted alongside this Memory. Should not contain the Memory's
     *                       position.
     * @return This MemoryBuilder.
     */
    MemoryBuilder otherPositions(List<BlockPos> otherPositions);

    /**
     * Convert this builder to an result, to be returned from {@link ServerProvider#onScreenClose(ScreenCloseContext)} )}.
     *
     * @param key Memory Key that this entry is located in.
     * @param position Position that this entry is located at.
     * @return An entry representing this builder and the given key and position.
     */
    DefaultProviderScreenClose.Result toResult(ResourceLocation key, BlockPos position);

    /**
     * Build the memory from the given data.
     *
     * @return A built memory with the supplied data.
     */
    Memory build();
}
