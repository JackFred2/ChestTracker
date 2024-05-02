package red.jackf.chesttracker.impl.storage.backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.impl.ChestTracker;
import red.jackf.chesttracker.impl.memory.MemoryBankImpl;
import red.jackf.chesttracker.impl.util.Constants;
import red.jackf.chesttracker.impl.util.FileUtil;
import red.jackf.chesttracker.impl.util.Misc;

import java.util.HashMap;

public class NbtBackend extends FileBasedBackend {
    private static final Logger LOGGER = LogManager.getLogger(ChestTracker.class.getCanonicalName() + "/NBT");

    @Override
    public @Nullable MemoryBankImpl load(String id) {
        var meta = loadMetadata(id);
        if (meta.isEmpty()) return null;
        var path = Constants.STORAGE_DIR.resolve(id + extension());
        var result = Misc.time(() -> FileUtil.loadFromNbt(MemoryBankImpl.DATA_CODEC, path));
        if (result.getFirst().isPresent()) {
            LOGGER.debug("Loaded {} in {}ns", path, result.getSecond());
            return new MemoryBankImpl(meta.get(), result.getFirst().get());
        } else {
            return new MemoryBankImpl(meta.get(), new HashMap<>());
        }
    }

    @Override
    public boolean save(MemoryBankImpl memoryBank) {
        LOGGER.debug("Saving {}", memoryBank.getId());
        memoryBank.getMetadata().updateModified();
        if (!saveMetadata(memoryBank.getId(), memoryBank.getMetadata())) return false;
        return FileUtil.saveToNbt(memoryBank.getMemories(), MemoryBankImpl.DATA_CODEC, Constants.STORAGE_DIR.resolve(memoryBank.getId() + extension()));
    }

    @Override
    public String extension() {
        return ".nbt";
    }
}
