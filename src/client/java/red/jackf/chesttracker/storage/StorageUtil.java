package red.jackf.chesttracker.storage;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.memory.Metadata;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.StringUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StorageUtil {
    private static final Logger NBT_LOGGER = ChestTracker.getLogger("NBT");
    private static final Logger LOGGER = ChestTracker.getLogger("Storage");
    private StorageUtil() {}

    private static Storage storage;

    private static Storage getStorage() {
        return storage;
    }

    static void setStorage(Storage storage) {
        StorageUtil.storage = storage;
    }

    public static void setup() {
        ChestTrackerConfig.INSTANCE.getConfig().storage.storageBackend.load();

        // storage saving hooks

        // on pause
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof PauseScreen) MemoryBank.save();
        });
    }

    public static Optional<Metadata> loadMetadata(String id) {
        if (MemoryBank.INSTANCE != null && MemoryBank.INSTANCE.getId().equals(id))
            return Optional.of(MemoryBank.INSTANCE.getMetadata().deepCopy());
        LOGGER.debug("Loading {} metadata using {}", id, storage.getClass().getSimpleName());
        return Optional.ofNullable(storage.getMetadata(id));
    }

    public static Collection<String> getAllIds() {
        return storage.getAllIds();
    }

    public static boolean exists(String id) {
        return getStorage().exists(id);
    }

    public static void delete(String id) {
        getStorage().delete(id);
    }

    public static Component getBackendLabel(String memoryBankId) {
        return storage.getDescriptionLabel(memoryBankId);
    }

    public static Optional<MemoryBank> load(String id) {
        if (MemoryBank.INSTANCE != null && id.equals(MemoryBank.INSTANCE.getId()))
            return Optional.of(MemoryBank.INSTANCE);
        LOGGER.debug("Loading {} using {}", id, storage.getClass().getSimpleName());
        var loaded = storage.load(id);
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
        storage.save(bank);
    }

    /**
     * Returns a list of relative file paths for files with a given extension in Chest Tracker's storage location
     * @param extension Extension to filter by e.g. '.json'
     * @return List of strings matching the file extension
     */
    public static List<String> getMemoryIdsFilteringFileExtension(String extension) {
        try(var stream = Files.walk(Constants.STORAGE_DIR)) {
            return stream.filter(path -> path.getFileName().toString().endsWith(extension))
                    .map(path -> StringUtil.formatPath(Constants.STORAGE_DIR.relativize(path)))
                    .map(s -> s.substring(0, s.length() - extension.length()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            ChestTracker.LOGGER.error(e);
            return Collections.emptyList();
        }
    }

    /**
     * Save an object to a path with a given codec as an NBT file
     * @param object Object to serialize
     * @param codec Codec to serialize said object with
     * @param path Path to save the object to
     * @return Whether the save was successful
     * @param <T> Type of the serialized object
     */
    public static <T> boolean saveToNbt(T object, Codec<T> codec, Path path) {
        try {
            Files.createDirectories(path.getParent());
            var tag = codec.encodeStart(NbtOps.INSTANCE, object).get();
            var result = tag.left();
            var err = tag.right();
            if (err.isPresent()) {
                throw new IOException("Error encoding to NBT %s".formatted(err.get()));
            } else if (result.isPresent() && result.get() instanceof CompoundTag compound) {
                NbtIo.writeCompressed(compound, path.toFile());
                return true;
            } else { //noinspection OptionalGetWithoutIsPresent
                throw new IOException("Error encoding to NBT: not a compound tag: %s".formatted(result.get()));
            }
        } catch(IOException ex) {
            NBT_LOGGER.error("Error saving object", ex);
            return false;
        }
    }

    /**
     * Load an NBT file to an object using a given codec
     * @param codec Codec to deserialize with
     * @param path Path to read from
     * @return An optional containing the deserialized object, or an empty optional if errored
     * @param <T> Type of deserialized object
     */
    public static <T> Optional<T> loadFromNbt(Codec<T> codec, Path path) {
        if (Files.isRegularFile(path)) {
            try {
                var tag = NbtIo.readCompressed(path.toFile());
                var loaded = codec.decode(NbtOps.INSTANCE, tag).get();
                if (loaded.right().isPresent()) {
                    throw new IOException("Invalid NBT: %s".formatted(loaded.right().get()));
                } else {
                    //noinspection OptionalGetWithoutIsPresent
                    return Optional.ofNullable(loaded.left().get().getFirst());
                }
            } catch (IOException ex) {
                NBT_LOGGER.error("Error loading object at {}", path, ex);
            }
        }
        return Optional.empty();
    }
}
