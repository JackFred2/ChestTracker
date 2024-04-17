package red.jackf.chesttracker.api.providers.defaults;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.providers.MemoryLocation;
import red.jackf.jackfredlib.api.base.ResultHolder;

public interface DefaultProviderMemoryLocation {
    Event<DefaultProviderMemoryLocation> EVENT = EventFactory.createWithPhases(DefaultProviderMemoryLocation.class, invokers -> cbs -> {
        for (DefaultProviderMemoryLocation invoker : invokers) {
            var result = invoker.getOverride(cbs);
            if (result.shouldTerminate()) {
                return result;
            }
        }

        return ResultHolder.empty();
    }, EventPhases.PRIORITY_PHASE,  EventPhases.DEFAULT_PHASE, EventPhases.FALLBACK_PHASE);

    ResultHolder<MemoryLocation> getOverride(ClientBlockSource cbs);
}
