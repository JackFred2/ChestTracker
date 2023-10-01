package red.jackf.chesttracker.memory;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.api.AfterPlayerDestroyBlock;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tries to keep memories valid, by tracking block breaking and timing out old memories
 */
public class MemoryIntegrity {
    private static final Logger LOGGER = ChestTracker.getLogger("Integrity");

    private MemoryIntegrity() {
    }

    private static final int TICKS_BETWEEN_ENTRY_REFILL = 600;
    private static final double PERIODIC_CHECK_RANGE_SQUARED = 32 * 32;
    public static final long UNKNOWN_LOADED_TIMESTAMP = -437822L;
    public static final long UNKNOWN_WORLD_TIMESTAMP = -437821L;
    public static final Instant UNKNOWN_REAL_TIMESTAMP = Instant.EPOCH;

    private static final List<Map.Entry<BlockPos, Memory>> currentEntryList = new ArrayList<>();
    private static long lastEntryCheckCompleteTick = -1L;
    private static int lastEntryListIndex = 0;

    public static void setup() {
        AfterPlayerDestroyBlock.EVENT.register(cbs -> {

            // Called when a player breaks a block, to remove memories that would be contained there
            if (MemoryBank.INSTANCE != null && MemoryBank.INSTANCE.getMetadata()
                    .getIntegritySettings().removeOnPlayerBlockBreak) {
            MemoryBank.INSTANCE.removeMemory(cbs.level().dimension().location(), cbs.pos());
                LOGGER.debug("Player Destroy Block: Removing {}@{}", cbs.pos().toShortString(), cbs.level().dimension()
                        .location());
            }
        });

        ClientTickEvents.END_WORLD_TICK.register(level -> {
            if (MemoryBank.INSTANCE == null) {
                lastEntryCheckCompleteTick = -1;
                currentEntryList.clear();
                return;
            }

            var integrity = MemoryBank.INSTANCE.getMetadata().getIntegritySettings();

            if (currentEntryList.isEmpty() && level.getGameTime() >= lastEntryCheckCompleteTick + TICKS_BETWEEN_ENTRY_REFILL) {
                var current = MemoryBank.INSTANCE.getMemories(level.dimension().location());
                if (current != null && !current.isEmpty()) {
                    LOGGER.debug("Refreshing entry list @ {}", level.getGameTime());
                    currentEntryList.addAll(current.entrySet());
                    lastEntryListIndex = 0;
                }
            }

            if (currentEntryList.isEmpty()) return;

            if (lastEntryListIndex >= currentEntryList.size()) {
                lastEntryCheckCompleteTick = level.getGameTime();
                currentEntryList.clear();
                return;
            }

            Map.Entry<BlockPos, Memory> currentEntry = currentEntryList.get(lastEntryListIndex++);
            BlockPos currentPos = currentEntry.getKey();
            Memory currentMemory = currentEntry.getValue();

            // check if time has expired
            // exempt named from the check
            if (!integrity.preserveNamed || currentMemory.name() == null) {
                final Long expirySeconds = integrity.memoryLifetime.seconds;
                if (expirySeconds != null) {
                    final long secondsPastExpiry = switch (integrity.lifetimeCountMode) {
                        case REAL_TIME ->
                                Duration.between(currentMemory.realTimestamp(), Instant.now()).toSeconds();
                        case WORLD_TIME -> (level.getGameTime() - currentMemory
                                .inGameTimestamp()) / SharedConstants.TICKS_PER_SECOND;
                        case LOADED_TIME -> (MemoryBank.INSTANCE.getMetadata().getLoadedTime() - currentMemory
                                .loadedTimestamp()) / SharedConstants.TICKS_PER_SECOND;
                    } - expirySeconds;

                    if (secondsPastExpiry > 0) {
                        MemoryBank.INSTANCE.removeMemory(level.dimension().location(), currentPos);
                        LOGGER.debug("Expiry: Removing {}@{}, {} seconds out of date", currentPos, level.dimension()
                                .location(), secondsPastExpiry);
                        return;
                    }
                }
            }

            // check if block is valid
            if (integrity.checkPeriodicallyForMissingBlocks) {
                var player = Minecraft.getInstance().player;
                if (player != null
                        && level.isLoaded(currentPos)
                        && currentPos.distSqr(player.blockPosition()) < PERIODIC_CHECK_RANGE_SQUARED) {
                    if (!(level.getBlockEntity(currentPos) instanceof MenuProvider)) {
                        MemoryBank.INSTANCE.removeMemory(level.dimension().location(), currentPos);
                        LOGGER.debug("Periodic Check: Removing {}@{}", currentPos, level.dimension()
                                .location());
                    }
                }
            }
        });
    }
}
