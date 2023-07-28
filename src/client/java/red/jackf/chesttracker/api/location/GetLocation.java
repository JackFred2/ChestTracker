package red.jackf.chesttracker.api.location;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import red.jackf.chesttracker.ChestTracker;

import java.util.Optional;

/**
 * Handles obtaining locations from specific events. These run through until the first non-empty Optional is produced;
 * following callbacks are not ran.
 */
public class GetLocation {
    private GetLocation() {}

    /**
     * Phase that gets called before all the others. Used in case the mod wants to override default behavior
     */
    public static final ResourceLocation PRIORITY_PHASE = ChestTracker.id("priority");

    /**
     * Phase that gets called after all the others. Used in the mod to provide the default right-click
     * in world behavior.
     */
    public static final ResourceLocation FALLBACK_PHASE = ChestTracker.id("fallback");

    /**
     * Called when a block is right-clicked in the world.
     */
    public static final Event<FromBlock> FROM_BLOCK = EventFactory.createWithPhases(FromBlock.class, listeners -> (player, level, hit) -> {
        for (FromBlock listener : listeners) {
            var result = listener.fromBlock(player, level, hit);
            if (result.isPresent()) return result;
        }
        return Optional.empty();
    }, PRIORITY_PHASE, Event.DEFAULT_PHASE, FALLBACK_PHASE);

    /**
     * Called when an item in the player's hand is right-clicked.
     */
    public static final Event<FromItem> FROM_ITEM = EventFactory.createWithPhases(FromItem.class, listeners -> (player, level, hand) -> {
        for (FromItem listener : listeners) {
            var result = listener.fromItem(player, level, hand);
            if (result.isPresent()) return result;
        }
        return Optional.empty();
    }, PRIORITY_PHASE, Event.DEFAULT_PHASE, FALLBACK_PHASE);

    /**
     * Called when an entity is right-clicked in the world.
     */
    public static final Event<FromEntity> FROM_ENTITY = EventFactory.createWithPhases(FromEntity.class, listeners -> (player, level, hand, hit) -> {
        for (FromEntity listener : listeners) {
            var result = listener.fromEntity(player, level, hand, hit);
            if (result.isPresent()) return result;
        }
        return Optional.empty();
    }, PRIORITY_PHASE, Event.DEFAULT_PHASE, FALLBACK_PHASE);

    public interface FromBlock {
        Optional<Location> fromBlock(Player player, ClientLevel level, BlockHitResult hit);
    }

    public interface FromItem {
        Optional<Location> fromItem(Player player, ClientLevel level, InteractionHand hand);
    }

    public interface FromEntity {
        Optional<Location> fromEntity(Player player, ClientLevel level, InteractionHand hand, EntityHitResult hit);
    }
}
