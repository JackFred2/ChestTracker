package red.jackf.chesttracker.storage.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.storage.StorageUtil;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.Timer;

public class NbtStorage implements FileBasedStorage {
    private static final Logger LOGGER = LogManager.getLogger(ChestTracker.class.getCanonicalName() + "/NBT");

    @Override
    public @Nullable MemoryBank load(String id) {
        var path = Constants.STORAGE_DIR.resolve(id + extension());
        var result = Timer.time(() -> StorageUtil.loadFromNbt(MemoryBank.CODEC, path));
        LOGGER.debug("Loaded {} in {}ns", path, result.getSecond());
        return result.getFirst().orElse(null);
    }

    @Override
    public void save(MemoryBank memoryBank) {
        LOGGER.debug("Saving {}", memoryBank.getId());
        memoryBank.getMetadata().updateModified();
        StorageUtil.saveToNbt(memoryBank, MemoryBank.CODEC, Constants.STORAGE_DIR.resolve(memoryBank.getId() + extension()));
    }

    @Override
    public String extension() {
        return ".nbt";
    }
}
