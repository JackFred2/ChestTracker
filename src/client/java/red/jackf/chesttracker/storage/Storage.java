package red.jackf.chesttracker.storage;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.memory.metadata.Metadata;
import red.jackf.chesttracker.storage.backend.Backend;

import java.util.Collection;
import java.util.Optional;

public class Storage {

    //////////////
    // INTERNAL //
    //////////////

    private static final Logger LOGGER = ChestTracker.getLogger("Storage");
    private static Backend backend;

    public static void setBackend(Backend backend) {
        Storage.backend = backend;
    }

    public static void setup() {
        ChestTrackerConfig.INSTANCE.getConfig().storage.storageBackend.load();

        // storage saving hooks

        // on pause
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof PauseScreen) MemoryBank.save();
        });
    }

    /////////
    // API //
    /////////

    public static Optional<Metadata> loadMetadata(String id) {
        if (MemoryBank.INSTANCE != null && MemoryBank.INSTANCE.getId().equals(id))
            return Optional.of(MemoryBank.INSTANCE.getMetadata().deepCopy());
        LOGGER.debug("Loading {} metadata using {}", id, backend.getClass().getSimpleName());
        return Optional.ofNullable(backend.getMetadata(id));
    }

    public static Collection<String> getAllIds() {
        return backend.getAllIds();
    }

    public static boolean exists(String id) {
        return backend.exists(id);
    }

    public static void delete(String id) {
        backend.delete(id);
    }

    public static Component getBackendLabel(String memoryBankId) {
        return backend.getDescriptionLabel(memoryBankId);
    }

    public static Optional<MemoryBank> load(String id) {
        if (MemoryBank.INSTANCE != null && id.equals(MemoryBank.INSTANCE.getId()))
            return Optional.of(MemoryBank.INSTANCE);
        LOGGER.debug("Loading {} using {}", id, backend.getClass().getSimpleName());
        var loaded = backend.load(id);
        if (loaded == null) return Optional.empty();
        loaded.setId(id);
        return Optional.of(loaded);
    }

    public static void save(MemoryBank bank) {
        if (bank == null) {
            LOGGER.warn("Tried to save null Memory Bank");
            return;
        }
        bank.getMetadata().updateModified();
        backend.save(bank);
    }
}
