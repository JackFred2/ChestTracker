package red.jackf.chesttracker.storage;

import red.jackf.chesttracker.memory.ItemMemory;

import java.util.HashMap;
import java.util.Map;

public class MemoryStorage implements Storage {
    private static final Map<String, ItemMemory> storage = new HashMap<>();

    @Override
    public ItemMemory load(String worldId) {
        return storage.computeIfAbsent(worldId, s -> new ItemMemory());
    }

    @Override
    public void save(ItemMemory memory) {
        storage.put(memory.getId(), memory);
    }
}
