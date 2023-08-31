package red.jackf.chesttracker.api.gui;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import red.jackf.jackfredlib.api.ResultHolder;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.location.Location;

/**
 * Gets the custom name of a container from a given location, block entity and screen.
 */
public interface GetCustomName {
    Event<GetCustomName> EVENT = EventFactory.createWithPhases(GetCustomName.class, invokers -> (location, screen) -> {
        for (var invoker : invokers) {
            var result = invoker.getName(location, screen);
            if (result.shouldTerminate()) return result;
        }
        return ResultHolder.empty();
    }, EventPhases.PRIORITY_PHASE, Event.DEFAULT_PHASE, EventPhases.FALLBACK_PHASE);

    ResultHolder<Component> getName(Location location, AbstractContainerScreen<?> screen);
}
