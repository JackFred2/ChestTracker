package red.jackf.chesttracker.storage.backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.FileUtil;
import red.jackf.chesttracker.util.Timer;

import java.util.HashMap;

public class NbtBackend extends FileBasedBackend {
    private static final Logger LOGGER = LogManager.getLogger(ChestTracker.class.getCanonicalName() + "/NBT");

    @Override
    public @Nullable MemoryBank load(String id) {
        var meta = loadMetadata(id);
        if (meta.isEmpty()) return null;
        var path = Constants.STORAGE_DIR.resolve(id + extension());
        var result = Timer.time(() -> FileUtil.loadFromNbt(MemoryBank.MEMORIES_CODEC, path));
        if (result.getFirst().isPresent()) {
            LOGGER.debug("Loaded {} in {}ns", path, result.getSecond());
            return new MemoryBank(meta.get(), result.getFirst().get());
        } else {
            return new MemoryBank(meta.get(), new HashMap<>());
        }
    }

    @Override
    public boolean save(MemoryBank memoryBank) {
        LOGGER.debug("Saving {}", memoryBank.getId());
        memoryBank.getMetadata().updateModified();
        if (!saveMetadata(memoryBank.getId(), memoryBank.getMetadata())) return false;
        return FileUtil.saveToNbt(memoryBank.getMemories(), MemoryBank.MEMORIES_CODEC, Constants.STORAGE_DIR.resolve(memoryBank.getId() + extension()));
    }

    @Override
    public String extension() {
        return ".nbt";
    }
}
