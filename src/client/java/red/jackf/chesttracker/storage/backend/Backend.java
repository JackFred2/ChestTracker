package red.jackf.chesttracker.storage.backend;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.memory.Metadata;
import red.jackf.chesttracker.storage.Storage;

import java.util.Collection;

/**
 * A handler for storing a memory bank in a black-box format
 */
public interface Backend {
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
     * Returns a small label to show at the top of the "edit memory bank" screen.
     * @param memoryBankId ID of a memory bank to generate a label for.
     * @return Component to show at the top of the edit memory bank screen.
     */
    default Component getDescriptionLabel(String memoryBankId) {
        return Component.empty();
    }

    /**
     * Return all IDs in this storage, such as all files, in no particular order.
     * @return All memory bank IDs accessible by this storage.
     */
    Collection<String> getAllIds();

    /**
     * Check whether an ID exists for this storage.
     * @param id ID to check existence for
     * @return Whether a memory bank by this ID exists.
     */
    default boolean exists(String id) {
        return getAllIds().contains(id);
    }

    /**
     * Returns just the metadata of a memory bank. If possible, load only the metadata instead of the whole file.
     * @param id ID of the memory bank to load
     * @return Metadata from the memory bank, or null if not.
     */
    @Nullable
    default Metadata getMetadata(String id) {
        var loaded = load(id);
        return loaded != null ? loaded.getMetadata() : null;
    }



    enum Type {
        JSON(new JsonBackend()),
        NBT(new NbtBackend()),
        MEMORY(new GameMemoryBackend());

        public final Backend instance;

        Type(Backend instance) {
            this.instance = instance;
        }

        public void load() {
            MemoryBank.unload();
            Storage.setBackend(instance);
        }
    }
}
