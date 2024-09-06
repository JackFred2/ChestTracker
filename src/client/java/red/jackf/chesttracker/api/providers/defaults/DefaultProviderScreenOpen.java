package red.jackf.chesttracker.api.providers.defaults;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import red.jackf.chesttracker.api.providers.ServerProvider;
import red.jackf.chesttracker.api.providers.context.ScreenOpenContext;

/**
 * Called by the default provider when a screen is opened in order to update the context. This is generally intended
 * to allow per-location features (such as renaming or ignoring) to work with overrides, such as when the ender chest
 * GUI is opened with a command instead of right clicking.
 *
 * @see red.jackf.chesttracker.impl.DefaultChestTrackerPlugin for implementation example
 * @see ScreenOpenContext
 * @see DefaultProvider
 */
public interface DefaultProviderScreenOpen {
    /**
     * Populate the screen context if needed. Currently, this is just setting the GUI's memory location.
     *
     * @param provider Provider currently being ran. Usually {@link DefaultProvider}.
     * @param context Context to populate.
     * @return Whether the context is finished being populated.
     */
    boolean handleScreenOpen(ServerProvider provider, ScreenOpenContext context);

    /**
     * Event for populating a {@link ScreenOpenContext}. Does not use phases. Not intended to be invoked by API users.
     */
    Event<DefaultProviderScreenOpen> EVENT = EventFactory.createArrayBacked(DefaultProviderScreenOpen.class, invokers -> (provider, context) -> {
        for (DefaultProviderScreenOpen invoker : invokers) {
            if (invoker.handleScreenOpen(provider, context)) {
                return true;
            }
        }

        return false;
    });
}
