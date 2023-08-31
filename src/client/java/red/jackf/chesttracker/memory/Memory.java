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

import java.time.Instant;
import java.util.*;

/**
 * List of information for a location.
 */
public final class Memory {
    public static final Codec<Memory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ExtraCodecs.COMPONENT.optionalFieldOf("name")
                            .forGetter(m -> Optional.ofNullable(m.name())),
                    ModCodecs.BLOCK_POS_STRING.listOf().optionalFieldOf("otherPositions")
                            .forGetter(m -> Optional.ofNullable(m.otherPositions.isEmpty() ? null : m.otherPositions)),
                    ItemStack.CODEC.listOf().fieldOf("items")
                            .forGetter(Memory::items),
                    ModCodecs.INSTANT.fieldOf("timestamp")
                            .forGetter(Memory::getTimestamp)
            ).apply(instance, (nameOpt, otherPositions, items, timestamp) -> new Memory(items, nameOpt.orElse(null), otherPositions.orElse(new ArrayList<>()), timestamp)));


    private final List<ItemStack> items;
    private final @Nullable Component name;
    private final List<BlockPos> otherPositions;
    private final Instant timestamp;

    private Memory(List<ItemStack> items, @Nullable Component name, List<BlockPos> otherPositions, Instant timestamp) {
        this.items = ImmutableList.copyOf(items);
        this.name = name;
        this.otherPositions = ImmutableList.copyOf(otherPositions);
        this.timestamp = timestamp;
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

    public Instant getTimestamp() {
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

        public Memory build() {
            return new Memory(items, name, otherPositions, Instant.now());
        }
    }
}
