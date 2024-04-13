package red.jackf.chesttracker.impl.storage.backend;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;
import red.jackf.chesttracker.impl.memory.MemoryBankImpl;
import red.jackf.chesttracker.impl.memory.metadata.Metadata;
import red.jackf.chesttracker.impl.storage.Storage;

import java.util.Collection;
import java.util.Optional;

/**
 * A handler for storing a memory bank in a black-box format
 */
public interface Backend {
    /**
     * Load a memory bank if it exists, or return null if not.
     *
     * @param id ID of the memory bank to load. This is guaranteed to be safe as part of a windows path.
     * @return Loaded Memory Bank, or null if not available.
     */
    @Nullable
    MemoryBankImpl load(String id);

    /**
     * Delete a memory from this storage. Not reversible.
     *
     * @param id ID of the memory bank to delete. If it does not exist, do nothing.
     */
    void delete(String id);

    /**
     * Save this memory bank and metadata. The ID is contained within the memory bank; use {@link MemoryBankImpl#getId()}.
     * If an error occurs, an exception should be logged, but should not crash the game.
     *
     * @param memoryBank Memory bank to save to this storage.
     */
    boolean save(MemoryBankImpl memoryBank);

    /**
     * Returns a small label to show at the top of the "edit memory bank" screen.
     *
     * @param memoryBankId ID of a memory bank to generate a label for.
     * @return Component to show at the top of the edit memory bank screen.
     */
    default Component getDescriptionLabel(String memoryBankId) {
        return Component.empty();
    }

    /**
     * Return all IDs in this storage, such as all files, in no particular order.
     *
     * @return All memory bank IDs accessible by this storage.
     */
    Collection<String> getAllIds();

    /**
     * Check whether an ID exists for this storage.
     *
     * @param id ID to check existence for
     * @return Whether a memory bank by this ID exists.
     */
    default boolean exists(String id) {
        return getAllIds().contains(id);
    }

    /**
     * Returns just the metadata of a memory bank. If possible, load only the metadata instead of the whole file.
     *
     * @param id ID of the memory bank to load
     * @return Metadata from the memory bank, or null if not.
     */
    Optional<Metadata> loadMetadata(String id);

    /**
     * Save just the metadata of a memory bank. If the memory bank does not exist, a new memory bank should be created.
     *
     * @param id ID of the memory bank to save
     * @param metadata Metadata object to save.
     * @return Whether the save was successful
     */
    boolean saveMetadata(String id, Metadata metadata);


    enum Type {
        JSON(new JsonBackend()),
        NBT(new NbtBackend()),
        MEMORY(new GameMemoryBackend());

        public final Backend instance;

        Type(Backend instance) {
            this.instance = instance;
        }

        public void load() {
            MemoryBankAccessImpl.ACCESS.unload();
            Storage.setBackend(instance);
        }
    }
}
