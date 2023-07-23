package red.jackf.chesttracker.storage;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.memory.MemoryBank;

public class StorageUtil {

    private StorageUtil() {}

    private static Storage instance;

    public static Storage getStorage() {
        return instance;
    }

    static void setStorage(Storage storage) {
        instance = storage;
    }

    public static void setup() {
        ChestTrackerConfig.INSTANCE.getConfig().storage.storageBackend.load();

        // storage saving hooks

        // on pause
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof PauseScreen) MemoryBank.save();
        });
    }

    /**
     * Load the appropriate memory based on the current context
     */
    public static void load(Minecraft mc) {
        if (!ChestTrackerConfig.INSTANCE.getConfig().memory.autoLoadMemories) return;
        var loadContext = LoadContext.get(mc);
        if (loadContext == null) {
            MemoryBank.unload();
        } else {
            ChestTracker.LOGGER.debug("Loading {} using {}", loadContext.id(), instance.getClass().getSimpleName());
            MemoryBank.loadOrCreate(loadContext.id(), MemoryBank.Metadata.from(loadContext));
        }
    }

}
