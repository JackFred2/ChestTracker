package red.jackf.chesttracker.api.providers.context;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.providers.ServerProvider;
import red.jackf.chesttracker.impl.providers.BlockPlacedContextImpl;

/**
 * Context for when the local player places a block.
 *
 * @see ServerProvider#onBlockPlaced(BlockPlacedContext)
 */
public interface BlockPlacedContext {
    /**
     * Get the block source for the block placement. This contains the raw level, position, block state, and <i>client-sided</i>
     * view of any block entity there.
     *
     * @return ClientBlockSource about the block that was placed.
     */
    ClientBlockSource getBlockSource();

    /**
     * Gets the ItemStack that is in the player's hand. This is before the stack has been decremented.
     *
     * @return ItemStack in the local player's hand.
     */
    ItemStack getPlacementStack();

    @ApiStatus.Internal
    static BlockPlacedContext create(ClientBlockSource cbs, ItemStack handStack) {
        return new BlockPlacedContextImpl(cbs, handStack.copy());
    }
}
