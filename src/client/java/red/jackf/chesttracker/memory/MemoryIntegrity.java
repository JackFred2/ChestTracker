package red.jackf.chesttracker.memory;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.api.AfterPlayerDestroyBlock;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tries to keep memories valid, by tracking block breaking and timing out old memories
 */
public class MemoryIntegrity {
    private static final Logger LOGGER = ChestTracker.getLogger("Integrity");
    private MemoryIntegrity() {}

    private static final int TICKS_BETWEEN_ENTRY_REFILL = 200;
    private static long lastEntryCheckCompleteTick = -1L;

    private static final List<Map.Entry<BlockPos, Memory>> currentEntryList = new ArrayList<>();
    private static int lastEntryListIndex = 0;

    public static void setup() {
        AfterPlayerDestroyBlock.EVENT.register((level, pos, state) -> {

            // Called when a player breaks a block, to remove memories that would be contained there
            if (MemoryBank.INSTANCE != null && MemoryBank.INSTANCE.getMetadata().getIntegritySettings().removeOnPlayerBlockBreak) {
                if (level instanceof ClientLevel clientLevel) {
                    MemoryBank.INSTANCE.removeMemory(clientLevel.dimension().location(), pos);
                    LOGGER.debug("Player Destroy Block: Removing {}@{}", pos.toShortString(), clientLevel.dimension().location());
                }
            }
        });

        ClientTickEvents.END_WORLD_TICK.register(client -> {
            if (MemoryBank.INSTANCE == null) {
                lastEntryCheckCompleteTick = -1;
                currentEntryList.clear();
                return;
            }

            var integrity = MemoryBank.INSTANCE.getMetadata().getIntegritySettings();

            if (currentEntryList.isEmpty() && client.getGameTime() >= lastEntryCheckCompleteTick + TICKS_BETWEEN_ENTRY_REFILL) {
                var current = MemoryBank.INSTANCE.getMemories(client.dimension().location());
                if (current != null && !current.isEmpty()) {
                    LOGGER.debug("Refreshing entry list @ {}", client.getGameTime());
                    currentEntryList.addAll(current.entrySet());
                    lastEntryListIndex = 0;
                }
            }

            if (currentEntryList.isEmpty()) return;

            if (lastEntryListIndex >= currentEntryList.size()) {
                lastEntryCheckCompleteTick = client.getGameTime();
                currentEntryList.clear();
                return;
            }

            var currentEntry = currentEntryList.get(lastEntryListIndex++);

            // exempt named from the check
            if (!integrity.preserveNamed || currentEntry.getValue().name() == null) {
                // check if time has expired
                var expirySeconds = integrity.memoryLifetime.seconds;
                if (expirySeconds != null) {
                    if (currentEntry.getValue().getTimestamp().plusSeconds(expirySeconds).isBefore(Instant.now())) {
                        MemoryBank.INSTANCE.removeMemory(client.dimension().location(), currentEntry.getKey());
                        LOGGER.debug("Expiry: Removing {}@{}", currentEntry.getKey(), client.dimension().location());
                    }
                }
            }
        });
    }
}
