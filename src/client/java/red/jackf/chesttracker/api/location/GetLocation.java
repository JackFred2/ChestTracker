package red.jackf.chesttracker.api.location;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.jackfredlib.api.ResultHolder;

/**
 * <p>Handles obtaining locations from specific events. These run through until the first non-empty Optional is produced;
 * following callbacks are not ran.</p>
 *
 * <p>Use cases for this are for custom inventories, such as server-side vaults, or not tracking certain containers, such
 * as Hypixel's loot box ender chest.</p>
 */
public class GetLocation {
    private GetLocation() {
    }

    /**
     * Called when a block is right-clicked in the world.
     */
    public static final Event<FromBlock> FROM_BLOCK = EventFactory.createWithPhases(FromBlock.class, listeners -> (player, level, hit) -> {
        for (FromBlock listener : listeners) {
            var result = listener.fromBlock(player, level, hit);
            if (result.shouldTerminate()) return result;
        }
        return ResultHolder.empty();
    }, EventPhases.PRIORITY_PHASE, Event.DEFAULT_PHASE, EventPhases.FALLBACK_PHASE);

    /**
     * Called when an item in the player's hand is right-clicked.
     */
    public static final Event<FromItem> FROM_ITEM = EventFactory.createWithPhases(FromItem.class, listeners -> (player, level, hand) -> {
        for (FromItem listener : listeners) {
            var result = listener.fromItem(player, level, hand);
            if (result.shouldTerminate()) return result;
        }
        return ResultHolder.empty();
    }, EventPhases.PRIORITY_PHASE, Event.DEFAULT_PHASE, EventPhases.FALLBACK_PHASE);

    /**
     * Called when an entity is right-clicked in the world.
     */
    public static final Event<FromEntity> FROM_ENTITY = EventFactory.createWithPhases(FromEntity.class, listeners -> (player, level, hand, hit) -> {
        for (FromEntity listener : listeners) {
            var result = listener.fromEntity(player, level, hand, hit);
            if (result.shouldTerminate()) return result;
        }
        return ResultHolder.empty();
    }, EventPhases.PRIORITY_PHASE, Event.DEFAULT_PHASE, EventPhases.FALLBACK_PHASE);

    public interface FromBlock {
        ResultHolder<Location> fromBlock(Player player, ClientLevel level, BlockPos pos);
    }

    public interface FromItem {
        ResultHolder<Location> fromItem(Player player, ClientLevel level, InteractionHand hand);
    }

    public interface FromEntity {
        ResultHolder<Location> fromEntity(Player player, ClientLevel level, InteractionHand hand, Entity entity);
    }
}
