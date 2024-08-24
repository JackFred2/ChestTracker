package red.jackf.chesttracker.impl.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import red.jackf.chesttracker.api.memory.Memory;
import red.jackf.chesttracker.api.memory.MemoryKey;
import red.jackf.chesttracker.api.providers.ProviderUtils;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;
import red.jackf.chesttracker.impl.memory.MemoryBankImpl;
import red.jackf.whereisit.client.api.RenderUtils;

import java.util.Map;
import java.util.Set;

public class NameRenderer {
    public static void setup() {
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hitResult) -> {
            MemoryBankAccessImpl.INSTANCE.getLoadedInternal().ifPresent(bank -> {
                if (!bank.getMetadata().getCompatibilitySettings().displayContainerNames || !ChestTrackerConfig.INSTANCE.instance().rendering.displayContainerNames)
                    return;
                bank.getKey(ProviderUtils.getPlayersCurrentKey()).ifPresent(key -> NameRenderer.renderNamesForKey(context, bank, key));
            });
            return true;
        });
    }

    private static void renderNamesForKey(WorldRenderContext context, MemoryBankImpl bank, MemoryKey key) {
        Map<BlockPos, Memory> named = key.getNamedMemories();
        final int maxRangeSq = ChestTrackerConfig.INSTANCE.instance().rendering.nameRange * ChestTrackerConfig.INSTANCE.instance().rendering.nameRange;
        Set<BlockPos> alreadyRendering = RenderUtils.getCurrentlyRenderedWithNames();
        for (var entry : named.entrySet()) {
            if (alreadyRendering.contains(entry.getKey())) continue;
            if (entry.getKey().distToCenterSqr(context.camera().getPosition()) < maxRangeSq) {
                Component name = entry.getValue().renderName();
                if (name == null) continue;
                RenderUtils.scheduleLabelRender(entry.getValue().getCenterPosition(entry.getKey()).add(0, 1, 0), entry.getValue().renderName());
            }
        }
    }
}
