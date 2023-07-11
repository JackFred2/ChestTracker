package red.jackf.chesttracker.world;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import red.jackf.chesttracker.ChestTracker;

/**
 * Since we technically don't immediately know what container is open, we try to smartly track it here.
 */
public class LocationTracking {
    @Nullable
    private static BlockHitResult lastHit = null;
    @Nullable
    private static Level lastLevel = null;

    public static void setup() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (!level.isClientSide || hand == InteractionHand.OFF_HAND) return InteractionResult.PASS;
            lastHit = hitResult;
            lastLevel = level;

            ChestTracker.LOGGER.debug(hand);
            ChestTracker.LOGGER.debug(lastLevel.dimension());
            ChestTracker.LOGGER.debug(lastLevel.dimensionTypeId());
            return InteractionResult.PASS;
        });
    }

    /**
     * Get the last known location interacted by the user, or null if not known.
     */
    @Nullable
    public static Location popLocation() {
        if (lastLevel == null || lastHit == null) return null;
        var loc = new Location(lastLevel.dimension(), lastHit.getBlockPos());
        lastHit = null;
        lastLevel = null;
        return loc;
    }
}
