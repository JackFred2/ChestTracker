package red.jackf.chesttracker.api.provider.def;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.provider.MemoryBuilder;
import red.jackf.chesttracker.api.provider.Provider;
import red.jackf.jackfredlib.api.base.ResultHolder;

/**
 * Create a memory from a given screen. Use the {@link red.jackf.chesttracker.api.provider.InteractionTracker} to check
 * for details of what was interacted with, and see whether your event handler applies to it.
 */
public interface DefaultMemoryCreator {
    Event<DefaultMemoryCreator> EVENT = EventFactory.createWithPhases(DefaultMemoryCreator.class, invokers -> (provider, screen) -> {
        for (DefaultMemoryCreator invoker : invokers) {
            var result = invoker.get(provider, screen);
            if (result.shouldTerminate()) return result;
        }
        return ResultHolder.empty();

    }, EventPhases.PRIORITY_PHASE, EventPhases.DEFAULT_PHASE, EventPhases.FALLBACK_PHASE);

    /**
     * Built a memory from a given screen and provider. Use {@link red.jackf.chesttracker.api.provider.InteractionTracker}
     * to get further details.
     *
     * @see red.jackf.chesttracker.api.provider.InteractionTracker
     * @see red.jackf.chesttracker.api.provider.ProviderUtils
     * @param provider Provider being used, usually but not guaranteed to be the default provider.
     * @param screen Screen to grab ItemStacks from and build a memory around.
     * @return A result holder possibly containing a partially built Memory. If a memory shouldn't be created, then
     *         a {@link ResultHolder#empty()} or {@link ResultHolder#pass()}, depending if your handler decidedly covers
     *         a block.
     */
    ResultHolder<MemoryBuilder.Entry> get(Provider provider, AbstractContainerScreen<?> screen);
}
