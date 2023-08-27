package red.jackf.chesttracker.storage.impl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.Timer;

import java.io.IOException;
import java.nio.file.Files;

public class NbtStorage implements FileBasedStorage {
    private static final Logger LOGGER = LogManager.getLogger(ChestTracker.class.getCanonicalName() + "/NBT");

    @Override
    public @Nullable MemoryBank load(String id) {
        var path = Constants.STORAGE_DIR.resolve(id + extension());
        var result = Timer.time(() -> {
            if (Files.isRegularFile(path)) {
                try {
                    var tag = NbtIo.readCompressed(path.toFile());
                    var loaded = MemoryBank.CODEC.decode(NbtOps.INSTANCE, tag).get();
                    if (loaded.right().isPresent()) {
                        throw new IOException("Invalid Memory Bank NBT: %s".formatted(loaded.right().get()));
                    } else {
                        //noinspection OptionalGetWithoutIsPresent
                        return loaded.left().get().getFirst();
                    }
                } catch (IOException ex) {
                    LOGGER.error("Error loading %s".formatted(path), ex);
                }
            }
            return null;
        });
        LOGGER.debug("Loaded {} in {}ns", path, result.getSecond());
        return result.getFirst();
    }

    @Override
    public void save(MemoryBank memoryBank) {
        LOGGER.debug("Saving {}", memoryBank.getId());
        memoryBank.getMetadata().updateModified();
        try {
            Files.createDirectories(Constants.STORAGE_DIR);
            var path = Constants.STORAGE_DIR.resolve(memoryBank.getId() + extension());
            var tag = MemoryBank.CODEC.encodeStart(NbtOps.INSTANCE, memoryBank).get();
            if (tag.right().isPresent()) {
                throw new IOException("Error encoding Memory Bank to Compound Tag: %s".formatted(tag.right().get()));
            } else //noinspection OptionalGetWithoutIsPresent
                if (tag.left().get() instanceof CompoundTag compound) {
                NbtIo.writeCompressed(compound, path.toFile());
            } else { //noinspection OptionalGetWithoutIsPresent
                throw new IOException("Error encoding Memory Bank: not a compound tag: %s".formatted(tag.left().get()));
            }
        } catch(IOException ex) {
            LOGGER.error("Error saving memories", ex);
        }
    }

    @Override
    public String extension() {
        return ".nbt";
    }
}
