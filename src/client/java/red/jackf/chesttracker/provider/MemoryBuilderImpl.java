package red.jackf.chesttracker.provider;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.provider.MemoryBuilder;
import red.jackf.chesttracker.memory.Memory;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MemoryBuilderImpl implements MemoryBuilder {
    private final List<ItemStack> items;
    private @Nullable Component name;
    private List<BlockPos> otherPositions = Collections.emptyList();
    private Block container;

    public MemoryBuilderImpl(List<ItemStack> items) {
        this.items = items;
    }

    public MemoryBuilderImpl withCustomName(@Nullable Component name) {
        this.name = name;
        return this;
    }

    @Override
    public MemoryBuilder inContainer(Block container) {
        this.container = container;
        return this;
    }

    public MemoryBuilderImpl otherPositions(List<BlockPos> otherPositions) {
        this.otherPositions = otherPositions;
        return this;
    }

    @Override
    public Entry toEntry(ResourceLocation key, BlockPos position) {
        return new Entry(key, position, this);
    }

    public Memory build(long loadedTimestamp, long inGameTimestamp, Instant realTimestamp) {
        return new Memory(items, name, otherPositions, Optional.ofNullable(container), loadedTimestamp, inGameTimestamp, realTimestamp);
    }
}
