package red.jackf.chesttracker.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Called after a player places a block, on the logical client. Client-sided alternative to the Fabric API version.
 */
public interface AfterPlayerPlaceBlock {
    Event<AfterPlayerPlaceBlock> EVENT = EventFactory.createArrayBacked(AfterPlayerPlaceBlock.class, invokers -> (level, pos, state, stack) -> {
        for (var invoker : invokers)
            invoker.afterPlayerPlaceBlock(level, pos, state, stack);
    });

    /**
     * Called after a player places a block on the client side.
     *
     * @param clientLevel Level the block was broken in.
     * @param pos Position the block was broken at.
     * @param state BlockState of the broken block.
     * @param placementStack ItemStack that was used to place.
     */
    void afterPlayerPlaceBlock(ClientLevel clientLevel, BlockPos pos, BlockState state, ItemStack placementStack);
}
