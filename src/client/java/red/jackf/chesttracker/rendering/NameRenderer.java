package red.jackf.chesttracker.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.provider.ProviderHandler;
import red.jackf.whereisit.client.api.RenderUtils;

import java.util.List;

public class NameRenderer {

    public static void setup() {
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hitResult) -> {
            if (MemoryBank.INSTANCE == null || !MemoryBank.INSTANCE.getMetadata().getCompatibilitySettings().displayContainerNames) return true;
            var currentKey = ProviderHandler.getCurrentKey();
            if (currentKey == null) return true;
            var named = MemoryBank.INSTANCE.getNamedMemories(currentKey);
            if (named == null) return true;
            final int maxRangeSq = ChestTrackerConfig.INSTANCE.instance().rendering.nameRange * ChestTrackerConfig.INSTANCE.instance().rendering.nameRange;
            var alreadyRendering = RenderUtils.getCurrentlyRenderedWithNames();
            for (var entry : named.entrySet()) {
                if (alreadyRendering.contains(entry.getKey())) continue;
                if (entry.getKey().distToCenterSqr(context.camera().getPosition()) < maxRangeSq) {
                    var pos = getRenderPos(entry.getKey(), entry.getValue().otherPositions());
                    RenderUtils.scheduleLabelRender(pos, entry.getValue().name());
                }
            }
            return true;
        });
    }

    private static Vec3 getRenderPos(BlockPos pos, List<BlockPos> otherPos) {
        var renderPos = pos.getCenter();
        for (BlockPos other : otherPos) {
            renderPos = renderPos.add(other.getCenter());
        }
        return renderPos.scale(1.0 / (1 + otherPos.size())).add(0, 1, 0);
    }
}
