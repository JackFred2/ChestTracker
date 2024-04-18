package red.jackf.chesttracker.impl.providers;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.memory.Memory;
import red.jackf.chesttracker.api.providers.MemoryBuilder;
import red.jackf.chesttracker.api.providers.defaults.DefaultProviderScreenClose;

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
    public DefaultProviderScreenClose.Result toResult(ResourceLocation key, BlockPos position) {
        return new DefaultProviderScreenClose.Result(key, position, this.build());
    }

    @Override
    public Memory build() {
        return new Memory(items, name, otherPositions, Optional.ofNullable(container), Memory.UNKNOWN_LOADED_TIMESTAMP, Memory.UNKNOWN_WORLD_TIMESTAMP, Memory.UNKNOWN_REAL_TIMESTAMP);
    }
}
