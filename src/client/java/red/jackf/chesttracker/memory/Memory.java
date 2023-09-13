package red.jackf.chesttracker.memory;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.util.ModCodecs;

import java.util.*;

/**
 * List of information for a location.
 */
public final class Memory {
    public static final Codec<Memory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemStack.CODEC.listOf().fieldOf("items")
                            .forGetter(Memory::items),
                    ExtraCodecs.COMPONENT.optionalFieldOf("name")
                            .forGetter(m -> Optional.ofNullable(m.name())),
                    ModCodecs.BLOCK_POS_STRING.listOf().optionalFieldOf("otherPositions")
                            .forGetter(m -> Optional.ofNullable(m.otherPositions.isEmpty() ? null : m.otherPositions)),
                    Codec.LONG.optionalFieldOf("timestamp")
                            .forGetter(m -> Optional.ofNullable(m.timestamp))
            ).apply(instance, (items, name, otherPositions, timestamp) -> new Memory(
                    items,
                    name.orElse(null),
                    timestamp.orElse(MemoryIntegrity.UNKNOWN_TIMESTAMP),
                    otherPositions.orElse(new ArrayList<>())
            )));


    private final List<ItemStack> items;
    private final @Nullable Component name;
    private final List<BlockPos> otherPositions;
    private final Long timestamp;

    private Memory(List<ItemStack> items, @Nullable Component name, @Nullable Long timestamp, List<BlockPos> otherPositions) {
        this.items = ImmutableList.copyOf(items);
        this.name = name;
        this.timestamp = timestamp;
        this.otherPositions = ImmutableList.copyOf(otherPositions);
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

    public List<BlockPos> getOtherPositions() {
        return otherPositions;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public static Builder builder(List<ItemStack> items) {
        return new Builder(items);
    }

    public static class Builder {

        private final List<ItemStack> items;
        @Nullable
        private Component name = null;

        private List<BlockPos> otherPositions = Collections.emptyList();

        public Builder(List<ItemStack> items) {
            this.items = items;
        }

        public Builder name(@Nullable Component name) {
            this.name = name;
            return this;
        }

        public Builder otherPositions(List<BlockPos> otherPositions) {
            this.otherPositions = otherPositions;
            return this;
        }

        public Memory build(long timestamp) {
            return new Memory(items, name, timestamp, otherPositions);
        }
    }
}
