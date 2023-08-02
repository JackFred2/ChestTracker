package red.jackf.chesttracker.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Called after a player destroys a block on the client-side.
 */
public interface AfterPlayerDestroyBlock {
    Event<AfterPlayerDestroyBlock> EVENT = EventFactory.createArrayBacked(AfterPlayerDestroyBlock.class, invokers -> (level, pos, state) -> {
        for (AfterPlayerDestroyBlock invoker : invokers)
            invoker.afterPlayerDestroyBlock(level, pos, state);
    });

    void afterPlayerDestroyBlock(LevelAccessor level, BlockPos pos, BlockState state);
}
