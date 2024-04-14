package red.jackf.chesttracker.api.providers.defaults;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.jackfredlib.api.base.ResultHolder;

/**
 * Called by the default provider in order to return a separate key for a given position in the world. This is used to
 * provide accurate previews to mods such as WTHIT and Jade for custom handled blocks such as ender chests.
 */
public interface DefaultProviderMemoryKeyOverride {
    /**
     * Event used to obtain an override. Uses the phases in {@link EventPhases}.
     */
    Event<DefaultProviderMemoryKeyOverride> EVENT = EventFactory.createWithPhases(DefaultProviderMemoryKeyOverride.class, invokers -> cbs -> {
        for (DefaultProviderMemoryKeyOverride invoker : invokers) {
            var result = invoker.getOverride(cbs);
            if (result.shouldTerminate()) {
                return result;
            }
        }

        return ResultHolder.empty();
    }, EventPhases.PRIORITY_PHASE,  EventPhases.DEFAULT_PHASE, EventPhases.FALLBACK_PHASE);

    /**
     * Any handlers should return{@link ResultHolder#value(Object)} if they can return an override from the given source,
     * {@link ResultHolder#pass()} if they don't have a result, or (not recommended) {@link ResultHolder#empty()} if they
     * conclusively don't have a result.
     * @param cbs Client block source containing the position to get an override for.
     * @return A result holder possibly containing an override.
     */
    ResultHolder<Pair<ResourceLocation, BlockPos>> getOverride(ClientBlockSource cbs);
}
