package red.jackf.chesttracker.world;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.Nullable;

/**
 * Since we technically don't immediately know what container is open, we try to smartly track it here.
 */
public class LocationTracking {
    @Nullable
    private static Location last = null;

    public static void setup() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (!level.isClientSide || hand == InteractionHand.OFF_HAND) return InteractionResult.PASS;
            last = new Location(
                    level.dimension(),
                    hitResult.getBlockPos(),
                    level.getBlockState(hitResult.getBlockPos())
            );
            return InteractionResult.PASS;
        });
    }

    /**
     * Get the last known location interacted by the user, or null if not known.
     */
    @Nullable
    public static Location popLocation() {
        var loc = last;
        last = null;
        return loc;
    }
}
