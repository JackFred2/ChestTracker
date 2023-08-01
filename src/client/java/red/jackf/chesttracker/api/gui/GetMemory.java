package red.jackf.chesttracker.api.gui;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.ResultHolder;
import red.jackf.chesttracker.api.location.Location;
import red.jackf.chesttracker.memory.Memory;

public interface GetMemory {
    Event<GetMemory> EVENT = EventFactory.createWithPhases(GetMemory.class, invokers -> (location, screen, level) -> {
        for (var invoker : invokers) {
            var result = invoker.createMemory(location, screen, level);
            if (result.shouldTerminate()) return result;
        }
        return ResultHolder.empty();
    }, EventPhases.PRIORITY_PHASE, Event.DEFAULT_PHASE, EventPhases.FALLBACK_PHASE);

    ResultHolder<Memory> createMemory(Location location, AbstractContainerScreen<?> screen, ClientLevel level);
}
