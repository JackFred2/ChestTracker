package red.jackf.chesttracker.memory;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.api.AfterPlayerDestroyBlock;
import red.jackf.chesttracker.api.location.GetLocation;

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

    private static final int TICKS_BETWEEN_ENTRY_REFILL = 200;
    private static long lastEntryCheckCompleteTick = -1L;

    private static final List<Map.Entry<BlockPos, Memory>> currentEntryList = new ArrayList<>();
    private static int lastEntryListIndex = 0;
    private static final double PERIODIC_CHECK_RANGE_SQUARED = 32 * 32;

    public static void setup() {
        AfterPlayerDestroyBlock.EVENT.register((level, pos, state) -> {

            // Called when a player breaks a block, to remove memories that would be contained there
            if (MemoryBank.INSTANCE != null && MemoryBank.INSTANCE.getMetadata()
                    .getIntegritySettings().removeOnPlayerBlockBreak) {
                if (level instanceof ClientLevel clientLevel) {
                    MemoryBank.INSTANCE.removeMemory(clientLevel.dimension().location(), pos);
                    LOGGER.debug("Player Destroy Block: Removing {}@{}", pos.toShortString(), clientLevel.dimension()
                            .location());
                }
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

            var currentEntry = currentEntryList.get(lastEntryListIndex++);

            // check if time has expired
            // exempt named from the check
            if (!integrity.preserveNamed || currentEntry.getValue().name() == null) {
                var expirySeconds = integrity.memoryLifetime.seconds;
                if (expirySeconds != null) {
                    if (currentEntry.getValue().getTimestamp().plusSeconds(expirySeconds).isBefore(Instant.now())) {
                        MemoryBank.INSTANCE.removeMemory(level.dimension().location(), currentEntry.getKey());
                        LOGGER.debug("Expiry: Removing {}@{}", currentEntry.getKey(), level.dimension().location());
                        return;
                    }
                }
            }

            // check if block is valid
            if (integrity.checkPeriodicallyForMissingBlocks) {
                var player = Minecraft.getInstance().player;
                if (player != null && level.isLoaded(currentEntry.getKey()) && currentEntry.getKey()
                        .distSqr(player.blockPosition()) < PERIODIC_CHECK_RANGE_SQUARED) {
                    var copyLocation = GetLocation.FROM_BLOCK.invoker().fromBlock(player, level, currentEntry.getKey());
                    if (!copyLocation.hasValue()
                            || !copyLocation.get().key().equals(level.dimension().location())
                            || !copyLocation.get().pos().equals(currentEntry.getKey())) {
                        MemoryBank.INSTANCE.removeMemory(level.dimension().location(), currentEntry.getKey());
                        LOGGER.debug("Periodic Check: Removing {}@{}", currentEntry.getKey(), level.dimension()
                                .location());
                    }
                }
            }
        });
    }
}
