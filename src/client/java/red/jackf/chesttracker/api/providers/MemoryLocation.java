package red.jackf.chesttracker.api.providers;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * A typed memory key and position pair for a memory. This can either be an 'in-world' location (which should be in the
 * same level as the local player i.e. 'minecraft:overworld'), or an 'override' (such as ender chests). This is gathered
 * when in several places in order to show correct information to the player, and change behavior of e.g. the block placed
 * event.
 */
public sealed interface MemoryLocation permits MemoryLocation.InWorld, MemoryLocation.Override {
    /**
     * @return The memory key defined for this location.
     */
    ResourceLocation memoryKey();

    /**
     * The block position defined for this location. This should generally be linked to a given world position if not
     * {@link #isOverride()}, but can be arbitrary if it is.
     *
     * @return The block position defined for this location.
     */
    BlockPos position();

    /**
     * Whether this memory location represents an override location, such as for an ender chest.
     *
     * @return Whether this location represents an override.
     */
    boolean isOverride();

    /**
     * Create a new memory location denoting an in-world location.
     *
     * @param memoryKey Memory key ID for the new location.
     * @param position  Block position for the new location. Should generally match a position in-world.
     * @return A new in-world memory location.
     */
    static MemoryLocation inWorld(ResourceLocation memoryKey, BlockPos position) {
        return new InWorld(memoryKey, position);
    }

    /**
     * Create a new memory location denoting an overridden location.
     *
     * @param memoryKey Memory key ID for the new location.
     * @param position  Block position for the new location.
     * @return A new override memory location.
     */
    static MemoryLocation override(ResourceLocation memoryKey, BlockPos position) {
        return new Override(memoryKey, position);
    }

    @ApiStatus.Internal
    record InWorld(ResourceLocation memoryKey, BlockPos position) implements MemoryLocation {
        @java.lang.Override
        public boolean isOverride() {
            return false;
        }
    }

    @ApiStatus.Internal
    record Override(ResourceLocation memoryKey, BlockPos position) implements MemoryLocation {
        @java.lang.Override
        public boolean isOverride() {
            return true;
        }
    }
}
