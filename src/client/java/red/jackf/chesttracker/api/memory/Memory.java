package red.jackf.chesttracker.api.memory;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.providers.MemoryBuilder;
import red.jackf.chesttracker.impl.util.Misc;
import red.jackf.chesttracker.impl.util.ModCodecs;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * <p>List of items and other details for a location. Obtained from memory keys in {@link MemoryBank}.</p>
 *
 * <p>Instances of this class should not be constructed manually, rather they should be built using the {@link MemoryBuilder}.</p>
 */
public final class Memory {
    public static final Instant UNKNOWN_REAL_TIMESTAMP = Instant.EPOCH;
    public static final long UNKNOWN_WORLD_TIMESTAMP = -437821L;
    public static final long UNKNOWN_LOADED_TIMESTAMP = -437822L;

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
                            Codec.LONG.optionalFieldOf("loadedTimestamp", UNKNOWN_LOADED_TIMESTAMP)
                                    .forGetter(Memory::loadedTimestamp),
                            Codec.LONG.optionalFieldOf("worldTimestamp", UNKNOWN_WORLD_TIMESTAMP)
                                    .forGetter(Memory::inGameTimestamp),
                            ExtraCodecs.INSTANT_ISO8601.optionalFieldOf("realTimestamp", UNKNOWN_REAL_TIMESTAMP)
                                    .forGetter(Memory::realTimestamp)
                    ).apply(instance, (items, name, otherPositions, container, loadedTimestamp, worldTimestamp, realTimestamp) -> new Memory(
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
    private final Optional<Block> container;
    private Long loadedTimestamp;
    private Long inGameTimestamp;
    private Instant realTimestamp;

    @ApiStatus.Internal
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

    /**
     * Whether this memory contains no items. Used internally to check if it should be removed.
     *
     * @return Whether this memory contains no items.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * A list of items contained by this memory. Does not include any {@link ItemStack#EMPTY} stacks, so slot information
     * is not preserved.
     *
     * @return A list of items contained in this memory.
     */
    public List<ItemStack> items() {
        return items;
    }

    /**
     * <p>A custom name for this memory usually obtained from renaming in an anvil. Null if no custom name is present.
     * Gets rendered in-world if {@link red.jackf.chesttracker.api.providers.ServerProvider#getPlayersCurrentKey(ClientLevel, LocalPlayer)}
     * is the same as this memory's key.</p>
     *
     * <p>This is usually obtained from the {@link red.jackf.chesttracker.api.gui.GetCustomName} event, which filters out
     * any screen titles with translatable components.</p>
     *
     * @return The custom name associated with this memory, or null if no custom name.
     */
    public @Nullable Component name() {
        return name;
    }

    /**
     * <p>A list of other block positions that are considered linked to this memory. This is used for getting correct
     * memories from the world for mods like WTHIT and Jade, as well as centering the name for the in-world names.</p>
     *
     * @return A list of other positions connected to this memory, not including its own. May be empty.
     */
    public List<BlockPos> otherPositions() {
        return otherPositions;
    }

    /**
     * Helper method for returning the center position of a memory, including it's other positions.
     *
     * @param origin Central position of this memory.
     * @return A position in the world regarded as the 'center'.
     */
    public Vec3 getCenterPosition(BlockPos origin) {
        return Misc.getAverageOffsetFrom(origin, this.otherPositions()).add(origin.getCenter());
    }

    /**
     * The block that this memory was in, or an empty optional if not recorded. Used to filter by container in the
     * Chest Tracker UI.
     *
     * @return The block that this memory was contained in, or an empty optional otherwise.
     */
    public Optional<Block> container() {
        return container;
    }

    /**
     * Refreshes the timestamps of this memory. These timestamps are checked as part of the memory integrity checking.
     *
     * @param memoryBankLoadedTime Time that the memory bank has been loaded, in ticks.
     * @param inGameTime The current world time.
     */
    public void touch(long memoryBankLoadedTime, long inGameTime) {
        this.loadedTimestamp = memoryBankLoadedTime;
        this.inGameTimestamp = inGameTime;
        this.realTimestamp = Instant.now();
    }

    /**
     * @return The time that this memory was saved in ticks, from the time the containing memory bank has been loaded.
     */
    public Long loadedTimestamp() {
        return loadedTimestamp;
    }

    /**
     * @return The world time that this memory was saved at, in ticks.
     */
    public Long inGameTimestamp() {
        return inGameTimestamp;
    }

    /**
     * @return The real life time that this memory was saved at, as calculated from {@link Instant#now()}.
     */
    public Instant realTimestamp() {
        return realTimestamp;
    }
}
