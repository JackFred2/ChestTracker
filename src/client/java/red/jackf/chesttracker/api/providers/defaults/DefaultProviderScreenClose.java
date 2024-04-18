package red.jackf.chesttracker.api.providers.defaults;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.memory.Memory;
import red.jackf.chesttracker.api.providers.InteractionTracker;
import red.jackf.chesttracker.api.providers.context.ScreenCloseContext;
import red.jackf.chesttracker.api.providers.ServerProvider;
import red.jackf.jackfredlib.api.base.ResultHolder;

/**
 * <p>Called by the default provider in order to build memories from different screens. Handlers should make use of the
 * {@link InteractionTracker} as well as the screen context in order to differentiate between what should be handled.</p>
 *
 * <p>Registering to this event instead of a custom provider should be done if possible as to allow other providers
 * to make use of the default provider's methods.</p>
 *
 * @see DefaultProvider
 */
public interface DefaultProviderScreenClose {
    /**
     * Event for building memories on screen close for the default provider. Uses the phases in {@link EventPhases} in order
     * to allow for overridable defaults; if this isn't enough use {@link Event#addPhaseOrdering(ResourceLocation, ResourceLocation)}
     * with your own.
     *
     * @see EventPhases
     */
    Event<DefaultProviderScreenClose> EVENT = EventFactory.createWithPhases(DefaultProviderScreenClose.class, listeners -> (provider, context) -> {
        for (DefaultProviderScreenClose listener : listeners) {
            var result = listener.createMemory(provider, context);
            if (result.shouldTerminate()) {
                return result;
            }
        }

        return ResultHolder.empty();
    }, EventPhases.PRIORITY_PHASE, EventPhases.DEFAULT_PHASE, EventPhases.FALLBACK_PHASE);

    /**
     * <p>This should create a {@link ResultHolder} depending on different conditions:</p>
     *
     * <p><b>If this event callback does not handle the given context</b>, it should return {@link ResultHolder#pass()}.</p>
     *
     * <p><b>If this callback does handle the given context</b>, it should return a {@link ResultHolder#value(Object)} containing
     * a given Memory built from the screen. If there is no memory to be created, it should return {@link ResultHolder#empty()}.</p>
     *
     * @param provider Provider instance loaded when creating a memory.
     * @param context  Screen closing context
     * @return A result holder possibly containing a given memory.
     * @see InteractionTracker#INSTANCE
     */
    ResultHolder<Result> createMemory(ServerProvider provider, ScreenCloseContext context);

    /**
     * Result holder for use when adding memories to a memory bank.
     *
     * @param key      Memory key to place the memory in
     * @param position Position in the key to place the memory
     * @param memory   Memory to add
     */
    record Result(ResourceLocation key, BlockPos position, Memory memory) {}
}
