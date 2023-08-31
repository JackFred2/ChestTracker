package red.jackf.chesttracker.world;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.location.GetLocation;
import red.jackf.chesttracker.api.location.Location;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.jackfredlib.api.ResultHolder;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;

/**
 * Since we technically don't immediately know what container is open, we try to smartly track it here.
 */
public class LocationTracking {
    @Nullable
    private static Location last = null;

    public static void setup() {
        // Event Setup
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (hand == InteractionHand.MAIN_HAND && level instanceof ClientLevel clientLevel) {
                setLocation(GetLocation.FROM_BLOCK.invoker()
                        .fromBlock(player, clientLevel, hitResult)
                        .getNullable());
            }
            return InteractionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (level instanceof ClientLevel clientLevel) {
                setLocation(GetLocation.FROM_ITEM.invoker()
                        .fromItem(player, clientLevel, hand)
                        .getNullable());
            }
            return InteractionResultHolder.pass(ItemStack.EMPTY);
        });

        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (level instanceof ClientLevel clientLevel) {
                setLocation(GetLocation.FROM_ENTITY.invoker()
                        .fromEntity(player, clientLevel, hand, hit)
                        .getNullable());
            }
            return InteractionResult.PASS;
        });

        // Mod Default Handlers
        setupDefaults();
    }

    /**
     * Get the last known location interacted by the user, or null if not known. Sets the location to null after the fact.
     */
    @Nullable
    public static Location popLocation() {
        var loc = last;
        last = null;
        return loc;
    }

    /**
     * Get the last known location interacted by the user, or null if not known. Does not clear the location.
     */
    @Nullable
    public static Location peekLocation() {
        return last;
    }

    private static void setLocation(@Nullable Location location) {
        ChestTracker.LOGGER.debug("Set last location to " + location);
        last = location;
    }

    private static void setupDefaults() {
        // only track block entities which can be renamed, which lines up with vanilla storage options
        // get a consistent 'root' inventory; solves double-saving chests
        GetLocation.FROM_BLOCK.register(EventPhases.FALLBACK_PHASE, (player, level, hit) -> {
            if (level.getBlockEntity(hit.getBlockPos()) instanceof MenuProvider) {
                var connected = ConnectedBlocksGrabber.getConnected(level, level.getBlockState(hit.getBlockPos()), hit.getBlockPos().immutable());
                return ResultHolder.value(new Location(level.dimension().location(), connected.get(0)));
            } else {
                return ResultHolder.pass();
            }
        });

        // Ender Chest
        GetLocation.FROM_BLOCK.register(Event.DEFAULT_PHASE, ((player, level, hit) -> {
            if (level.getBlockState(hit.getBlockPos()).is(Blocks.ENDER_CHEST)) {
                return ResultHolder.value(new Location(MemoryBank.ENDER_CHEST_KEY, BlockPos.ZERO));
            } else {
                return ResultHolder.pass();
            }
        }));
    }
}
