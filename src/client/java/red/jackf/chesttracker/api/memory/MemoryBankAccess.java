package red.jackf.chesttracker.api.memory;

import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;
import red.jackf.jackfredlib.client.api.gps.Coordinate;

import java.util.Optional;

/**
 * Utilities for working with Memory Banks. This allows for loading and unloading of banks for providers, as well as
 * mod access to the currently loaded.
 */
public interface MemoryBankAccess {
    /**
     * The current instance of memory bank access.
     */
    MemoryBankAccess INSTANCE = MemoryBankAccessImpl.INSTANCE;

    /**
     * Tries to load a memory bank by the given ID, or creates one if none exist.
     *
     * @param memoryBankId     File ID to try and load.
     * @param userFriendlyName Default user-facing name if creating a new memory bank.
     * @return Whether loading or creating the memory bank was successful.
     */
    boolean loadOrCreate(String memoryBankId, String userFriendlyName);

    /**
     * Load a memory bank from a given coordinate using default behavior. This will use a user's default overrides if
     * present.
     *
     * @param coordinate Coordinate to load from.
     * @return Whether loading or creating the memory bank was successful.
     */
    boolean loadWithDefaults(Coordinate coordinate);

    /**
     * Save and unload the current memory bank, if one is loaded.
     *
     * @return Whether a memory bank was successfully unloaded. Returns false if no bank was loaded.
     */
    boolean unload();

    /**
     * Returns an optional possibly containing the current memory bank.
     *
     * @return The currently loaded memory bank, or an empty optional if none loaded.
     */
    Optional<MemoryBank> getLoaded();
}
