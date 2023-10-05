package red.jackf.chesttracker.api.provider;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.memory.Memory;
import red.jackf.chesttracker.provider.MemoryBuilderImpl;

import java.time.Instant;
import java.util.List;

/**
 * A partially built Memory to be added to the current Memory Bank.
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
     * Adds a set of other positions to be highlighted when the Memory is highlighted (think double chests).
     *
     * @param otherPositions Other positions to be highlighted alongside this Memory. Should not contain the Memory's
     *                       position.
     * @return This MemoryBuilder.
     */
    MemoryBuilder otherPositions(List<BlockPos> otherPositions);

    /**
     * Convert this builder to an entry, to be returned from {@link Provider#createMemory(AbstractContainerScreen)}.
     *
     * @param key Memory Key that this entry is located in.
     * @param position Position that this entry is located at.
     * @return An entry representing this builder and the given key and position.
     */
    MemoryBuilder.Entry toEntry(ResourceLocation key, BlockPos position);

    @ApiStatus.Internal
    Memory build(long loadedTimestamp, long inGameTimestamp, Instant realTimestamp);

    record Entry(ResourceLocation key, BlockPos position, MemoryBuilder memory) {}
}
