package red.jackf.chesttracker.api.providers.defaults;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.jackfredlib.api.base.ResultHolder;

public interface DefaultProviderMemoryKeyOverride {
    Event<DefaultProviderMemoryKeyOverride> EVENT = EventFactory.createWithPhases(DefaultProviderMemoryKeyOverride.class, invokers -> cbs -> {
        for (DefaultProviderMemoryKeyOverride invoker : invokers) {
            var result = invoker.getOverride(cbs);
            if (result.shouldTerminate()) {
                return result;
            }
        }

        return ResultHolder.empty();
    }, EventPhases.PRIORITY_PHASE,  EventPhases.DEFAULT_PHASE, EventPhases.FALLBACK_PHASE);

    ResultHolder<Pair<ResourceLocation, BlockPos>> getOverride(ClientBlockSource cbs);
}
