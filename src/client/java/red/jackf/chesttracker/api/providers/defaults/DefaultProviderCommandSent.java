package red.jackf.chesttracker.api.providers.defaults;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import red.jackf.chesttracker.api.providers.ServerProvider;

public interface DefaultProviderCommandSent {
    Event<DefaultProviderCommandSent> EVENT = EventFactory.createArrayBacked(DefaultProviderCommandSent.class, invokers -> (provider, command) -> {
        for (DefaultProviderCommandSent invoker : invokers) {
            invoker.onDefaultCommandSend(provider, command);
        }
    });

    void onDefaultCommandSend(ServerProvider provider, String command);
}
