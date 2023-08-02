package red.jackf.chesttracker.memory;

import net.minecraft.client.multiplayer.ClientLevel;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.api.AfterPlayerDestroyBlock;

/**
 * Tries to keep memories valid, by tracking block breaking and timing out old memories
 */
public class MemoryIntegrity {
    private MemoryIntegrity() {}

    public static void setup() {
        AfterPlayerDestroyBlock.EVENT.register((level, pos, state) -> {
            if (level instanceof ClientLevel clientLevel) {
                if (MemoryBank.INSTANCE != null) {
                    MemoryBank.INSTANCE.removeMemory(clientLevel.dimension().location(), pos);
                    ChestTracker.LOGGER.debug("Removing {}@{}", pos.toShortString(), clientLevel.dimension().location());
                }
            }
        });
    }
}
