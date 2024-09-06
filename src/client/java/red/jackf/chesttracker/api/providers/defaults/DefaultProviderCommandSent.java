package red.jackf.chesttracker.api.providers.defaults;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import red.jackf.chesttracker.api.providers.ServerProvider;

/**
 * Called by the default provider when the user sends a command. Used internally to set state for ender chest commands.
 *
 * @see DefaultProvider
 */
public interface DefaultProviderCommandSent {
    /**
     * Event for handling default user commands.
     */
    Event<DefaultProviderCommandSent> EVENT = EventFactory.createArrayBacked(DefaultProviderCommandSent.class, invokers -> (provider, command) -> {
        for (DefaultProviderCommandSent invoker : invokers) {
            invoker.onDefaultCommandSend(provider, command);
        }
    });

    /**
     * Called by the default provider when the user runs a command.
     *
     * @param provider Provider being used; this is usually a {@link DefaultProvider} instance.
     * @param command Command that the user sent, without the slash
     */
    void onDefaultCommandSend(ServerProvider provider, String command);
}
