package red.jackf.chesttracker.storage.backend;

import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.memory.MemoryBank;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GameMemoryBackend implements Backend {
    private static final Map<String, MemoryBank> storage = new HashMap<>();

    @Nullable
    @Override
    public MemoryBank load(String id) {
        return storage.get(id);
    }

    @Override
    public void delete(String id) {
        storage.remove(id);
    }

    @Override
    public void save(MemoryBank memoryBank) {
        storage.put(memoryBank.getId(), memoryBank);
    }

    @Override
    public Collection<String> getAllIds() {
        return storage.keySet();
    }
}
