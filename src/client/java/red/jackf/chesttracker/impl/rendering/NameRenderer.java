package red.jackf.chesttracker.impl.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
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
                if (!bank.getMetadata().getCompatibilitySettings().displayContainerNames)
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

                BlockPos blockPos = entry.getKey();
                Vec3 facingOffset = getFacingOffset(blockPos);
                Vec3 renderPos = entry.getValue().getCenterPosition(blockPos).add(facingOffset);
                // RenderUtils.scheduleLabelRender(entry.getValue().getCenterPosition(entry.getKey()).add(1, 0, 0), entry.getValue().renderName());
            }
        }
    }

    private static Vec3 getFacingOffset(BlockPos blockPos) {
        BlockState blockState;
        blockState = blockPos().getBlockState(blockPos);
        Direction facing = blockState.getValue(BlockStateProperties.FACING);

        return switch (facing) {
            case NORTH -> new Vec3(0, 0, -1);
            case SOUTH -> new Vec3(0, 0, 1);
            case WEST -> new Vec3(-1, 0, 0);
            case EAST -> new Vec3(1, 0, 0);
            case UP -> new Vec3(0, 1, 0);
            case DOWN -> new Vec3(0, -1, 0);
            default -> Vec3.ZERO;
        };
    }
}
