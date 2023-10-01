package red.jackf.chesttracker.provider;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.provider.MemoryBuilder;
import red.jackf.chesttracker.memory.Memory;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class MemoryBuilderImpl implements MemoryBuilder {
    private final List<ItemStack> items;
    private @Nullable Component name;
    private List<BlockPos> otherPositions = Collections.emptyList();

    public MemoryBuilderImpl(List<ItemStack> items) {
        this.items = items;
    }

    public MemoryBuilderImpl withCustomName(@Nullable Component name) {
        this.name = name;
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
        return new Memory(items, name, otherPositions, loadedTimestamp, inGameTimestamp, realTimestamp);
    }
}
