package red.jackf.chesttracker.memory;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.util.ModCodecs;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * List of information for a location.
 */
public final class Memory {
    public static final Codec<Memory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                            ItemStack.CODEC.listOf().fieldOf("items")
                                    .forGetter(Memory::items),
                            ComponentSerialization.CODEC.optionalFieldOf("name")
                                                  .forGetter(m -> Optional.ofNullable(m.name)),
                            ModCodecs.BLOCK_POS_STRING.listOf().optionalFieldOf("otherPositions", Collections.emptyList())
                                    .forGetter(Memory::otherPositions),
                            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("container")
                                                   .forGetter(Memory::container),
                            Codec.LONG.optionalFieldOf("loadedTimestamp", MemoryIntegrity.UNKNOWN_LOADED_TIMESTAMP)
                                    .forGetter(Memory::loadedTimestamp),
                            Codec.LONG.optionalFieldOf("worldTimestamp", MemoryIntegrity.UNKNOWN_WORLD_TIMESTAMP)
                                    .forGetter(Memory::inGameTimestamp),
                            ExtraCodecs.INSTANT_ISO8601.optionalFieldOf("realTimestamp", MemoryIntegrity.UNKNOWN_REAL_TIMESTAMP)
                                    .forGetter(Memory::realTimestamp)
                    )
                    .apply(instance, (items, name, otherPositions, container, loadedTimestamp, worldTimestamp, realTimestamp) -> new Memory(
                            items,
                            name.orElse(null),
                            otherPositions,
                            container,
                            loadedTimestamp,
                            worldTimestamp,
                            realTimestamp
                    )));


    private final List<ItemStack> items;
    private final @Nullable Component name;
    private final List<BlockPos> otherPositions;
    private final Long loadedTimestamp;
    private final Long inGameTimestamp;
    private final Instant realTimestamp;
    private final Optional<Block> container;

    public Memory(
            List<ItemStack> items,
            @Nullable Component name,
            List<BlockPos> otherPositions,
            Optional<Block> container,
            long loadedTimestamp,
            long inGameTimestamp,
            Instant realTimestamp) {
        this.items = ImmutableList.copyOf(items);
        this.name = name;
        this.otherPositions = ImmutableList.copyOf(otherPositions);
        this.loadedTimestamp = loadedTimestamp;
        this.inGameTimestamp = inGameTimestamp;
        this.realTimestamp = realTimestamp;
        this.container = container;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<ItemStack> items() {
        return items;
    }

    public @Nullable Component name() {
        return name;
    }

    public List<BlockPos> otherPositions() {
        return otherPositions;
    }

    public Long loadedTimestamp() {
        return loadedTimestamp;
    }

    public Long inGameTimestamp() {
        return inGameTimestamp;
    }

    public Instant realTimestamp() {
        return realTimestamp;
    }

    public Optional<Block> container() {
        return container;
    }
}
