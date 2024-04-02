package red.jackf.chesttracker.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import red.jackf.chesttracker.api.ClientBlockSource;

/**
 * Called after a player destroys a block on the client-side.
 */
public interface AfterPlayerDestroyBlock {
    Event<AfterPlayerDestroyBlock> EVENT = EventFactory.createArrayBacked(AfterPlayerDestroyBlock.class, invokers -> cbs -> {
        for (AfterPlayerDestroyBlock invoker : invokers)
            invoker.afterPlayerDestroyBlock(cbs);
    });

    /**
     * Called after the local client player destroys a block.
     * @param cbs A ClientBlockSource containing the details on where the block was broken.
     */
    void afterPlayerDestroyBlock(ClientBlockSource cbs);
}
