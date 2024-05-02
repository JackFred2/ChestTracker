package red.jackf.chesttracker.api.providers.defaults;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.providers.MemoryLocation;
import red.jackf.jackfredlib.api.base.ResultHolder;

/**
 * <p>Called by the default provider in order to get a location for a memory from a given world position. This is used
 * for lots of compatibility features, including auto-adding placed blocks, enabling manual mode and client-side
 * container names, WTHIT and Jade integration, and more.</p>
 *
 * <p>The returned location may either be specified as an 'in-world' location (which should correspond to a location in the
 * world the player is in), or as an 'override' location (such as ender chests).</p>
 *
 * <p>Registering to this event instead of a custom provider should be done if possible as to allow other providers
 * to make use of the default provider's methods.</p>
 *
 * @see MemoryLocation
 * @see DefaultProvider
 */
public interface DefaultProviderMemoryLocation {
    /**
     * Event for getting a memory location from a given position in-world. Uses the phases in {@link EventPhases} in order
     * to allow for overridable defaults; if this isn't enough use {@link Event#addPhaseOrdering(ResourceLocation, ResourceLocation)}
     * with your own.
     *
     * @see EventPhases
     */
    Event<DefaultProviderMemoryLocation> EVENT = EventFactory.createWithPhases(DefaultProviderMemoryLocation.class, invokers -> cbs -> {
        for (DefaultProviderMemoryLocation invoker : invokers) {
            var result = invoker.getOverride(cbs);
            if (result.shouldTerminate()) {
                return result;
            }
        }

        return ResultHolder.empty();
    }, EventPhases.PRIORITY_PHASE, EventPhases.DEFAULT_PHASE, EventPhases.FALLBACK_PHASE);

    /**
     * <p>This should create an appropriately-typed {@link MemoryLocation} corresponding to a Memory from a given world
     * position, or a {@link ResultHolder#pass()} otherwise.</p>
     *
     * <p>This callback should return a {@link ResultHolder#pass()} if it does not handle the given block source. If it
     * does, it should return a {@link ResultHolder#value(Object)} containing a relevant MemoryLocation.</p>
     *
     * @param cbs Block source to obtain a memory location from.
     * @return A result holder possibly containing memory location.
     */
    ResultHolder<MemoryLocation> getOverride(ClientBlockSource cbs);
}
