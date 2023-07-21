package red.jackf.chesttracker.storage;

import dev.isxander.yacl3.api.OptionGroup;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.memory.MemoryBank;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Handles saving/loading for memoryBank
 */
public interface Storage {
    /**
     * Load a memory bank if it exists, or return null if not.
     * @param id ID of the memory bank to load. This is guaranteed to be safe as part of a windows path.
     * @return Loaded Memory Bank, or null if not available.
     */
    @Nullable
    MemoryBank load(String id);

    /**
     * Delete a memory from this storage. Not reversible.
     * @param id ID of the memory bank to delete. If it does not exist, do nothing.
     */
    void delete(String id);

    /**
     * Save this memory bank. The ID is contained within the memory bank; use {@link MemoryBank#getId()}.
     * If an error occurs, an exception should be logged, but should not crash the game.
     * @param memoryBank Memory bank to save to this storage.
     */
    void save(MemoryBank memoryBank);

    /**
     * Add any storage-specific options to the config GUI. Generally used with {@link dev.isxander.yacl3.api.LabelOption}s
     * to add specific info.
     * @param memoryBank Memory bank to parse options for
     * @param builder YACL group builder to append options to.
     */
    void appendOptionsToSettings(MemoryBank memoryBank, OptionGroup.Builder builder);

    /**
     * Return all IDs in this storage, such as all files, in no particular order.
     * @return All memory bank IDs accessible by this storage.
     */
    Collection<String> getAllIds();

    /**
     * Returns just the metadata of a memory bank. If possible, load only the metadata instead of the whole file.
     * @param id ID of the memory bank to load
     * @return Metadata from the memory bank, or null if not.
     */
    @Nullable
    default MemoryBank.Metadata getMetadata(String id) {
        var loaded = load(id);
        return loaded != null ? loaded.getMetadata() : null;
    }

    enum Backend {
        JSON(JsonStorage::new),
        MEMORY(GameMemoryStorage::new);

        private final Supplier<Storage> constructor;

        Backend(Supplier<Storage> constructor) {
            this.constructor = constructor;
        }

        public void load() {
            // var id = MemoryBank.INSTANCE != null ? MemoryBank.INSTANCE.getId() : null;
            MemoryBank.unload();
            StorageUtil.setStorage(constructor.get());
            // if (id != null) MemoryBank.load(id);
        }
    }
}
