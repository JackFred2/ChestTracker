package red.jackf.chesttracker.api.gui;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.jackfredlib.api.base.ResultHolder;

/**
 * Gets the custom name of a container from a given screen.
 */
public interface GetCustomName {
    Event<GetCustomName> EVENT = EventFactory.createWithPhases(GetCustomName.class, invokers -> (screen) -> {
        for (var invoker : invokers) {
            var result = invoker.getName(screen);
            if (result.shouldTerminate()) return result;
        }
        return ResultHolder.empty();
    }, EventPhases.PRIORITY_PHASE, Event.DEFAULT_PHASE, EventPhases.FALLBACK_PHASE);

    /**
     * Get a custom (player-defined) name of a screen. This method should return:
     * <li>{@link ResultHolder#pass()} if this handler does not handle the screen</li>
     * <li>{@link ResultHolder#empty()} if this handler does the screen, but it hasn't been renamed</li>
     * <li>{@link ResultHolder#value(Object)} if this handler does handle the screen, and it has
     * been renamed.</li>
     *
     * @see red.jackf.chesttracker.api.providers.InteractionTracker Interaction Tracker, containing information
     * about an interacted block if needed.
     * @param screen Screen to pull the custom name from.
     * @return A result holder possibly containing a custom name for the given screen.
     */
    ResultHolder<Component> getName(AbstractContainerScreen<?> screen);
}
