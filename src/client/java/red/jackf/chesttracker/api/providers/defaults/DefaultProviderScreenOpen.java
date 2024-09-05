package red.jackf.chesttracker.api.providers.defaults;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import red.jackf.chesttracker.api.providers.ServerProvider;
import red.jackf.chesttracker.api.providers.context.ScreenOpenContext;

public interface DefaultProviderScreenOpen {
    boolean handleScreenOpen(ServerProvider provider, ScreenOpenContext context);

    Event<DefaultProviderScreenOpen> EVENT = EventFactory.createArrayBacked(DefaultProviderScreenOpen.class, invokers -> (provider, context) -> {
        for (DefaultProviderScreenOpen invoker : invokers) {
            if (invoker.handleScreenOpen(provider, context)) {
                return true;
            }
        }

        return false;
    });
}
