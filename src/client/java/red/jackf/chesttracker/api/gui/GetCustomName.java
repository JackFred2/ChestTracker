package red.jackf.chesttracker.api.gui;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.jackfredlib.api.base.ResultHolder;

/**
 * Gets the custom name of a container from a given source, block entity and screen.
 */
public interface GetCustomName {
    Event<GetCustomName> EVENT = EventFactory.createWithPhases(GetCustomName.class, invokers -> (source, screen) -> {
        for (var invoker : invokers) {
            var result = invoker.getName(source, screen);
            if (result.shouldTerminate()) return result;
        }
        return ResultHolder.empty();
    }, EventPhases.PRIORITY_PHASE, Event.DEFAULT_PHASE, EventPhases.FALLBACK_PHASE);

    /**
     * Get a custom (player-defined) name of a screen. This method should return:
     * <li>{@link ResultHolder#pass()} if this handler does not handle the screen + source combination</li>
     * <li>{@link ResultHolder#empty()} if this handler does the screen + source combination, but it hasn't been renamed</li>
     * <li>{@link ResultHolder#value(Object)} if this handler does handle the screen + source combination, and it has
     * been renamed.</li>
     *
     * @param source ClientBlockSource containing the details of where the block was interacted with.
     * @param screen Screen to pull the custom name from
     * @return A result holder possibly containing a custom name for the given screen.
     */
    ResultHolder<Component> getName(ClientBlockSource source, AbstractContainerScreen<?> screen);
}
