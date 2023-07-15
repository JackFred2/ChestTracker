package red.jackf.chesttracker.storage;

import red.jackf.chesttracker.memory.ItemMemory;

/**
 * Handles saving/loading for memory
 */
public interface Storage {

    Storage INSTANCE = new JsonStorage();

    ItemMemory load(String worldId);

    void save(ItemMemory memory);
}
