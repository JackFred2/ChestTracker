package red.jackf.chesttracker.storage;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import org.apache.logging.log4j.LogManager;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StorageUtil {
    private static final Logger NBT_LOGGER = LogManager.getLogger(ChestTracker.class.getCanonicalName() + "/NBT");
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
     * Automatically get and load a default memory based on the current context and connection-specific settings
     */
    public static void load() {
        // if (!ChestTrackerConfig.INSTANCE.getConfig().memory.autoLoadMemories) return;
        var loadContext = LoadContext.get();

        // not in-game; don't load
        if (loadContext == null) {
            MemoryBank.unload();
        } else {
            var settings = ConnectionSettings.getOrCreate(loadContext.connectionId());
            var id = settings.memoryBankIdOverride().orElse(loadContext.connectionId());
            ChestTracker.LOGGER.debug("Loading {} using {}", id, instance.getClass().getSimpleName());
            MemoryBank.loadOrCreate(id, Metadata.blankWithName(loadContext.name()));
        }
    }

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
