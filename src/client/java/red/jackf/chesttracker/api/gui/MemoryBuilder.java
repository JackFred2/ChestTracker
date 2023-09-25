package red.jackf.chesttracker.api.gui;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.memory.Memory;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class MemoryBuilder {

    private final List<ItemStack> items;
    @Nullable
    private Component name = null;

    private List<BlockPos> otherPositions = Collections.emptyList();

    public MemoryBuilder(List<ItemStack> items) {
        this.items = items;
    }

    public MemoryBuilder name(@Nullable Component name) {
        this.name = name;
        return this;
    }

    public MemoryBuilder otherPositions(List<BlockPos> otherPositions) {
        this.otherPositions = otherPositions;
        return this;
    }

    public Memory build(long loadedTimestamp, long inGameTimestamp, Instant realTimestamp) {
        return new Memory(items, name, otherPositions, loadedTimestamp, inGameTimestamp, realTimestamp);
    }
}
