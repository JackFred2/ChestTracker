package red.jackf.chesttracker.storage;

import dev.isxander.yacl3.api.OptionGroup;
import red.jackf.chesttracker.memory.MemoryBank;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryStorage implements Storage {
    private static final Map<String, MemoryBank> storage = new HashMap<>();

    @Override
    public MemoryBank load(String id) {
        return storage.computeIfAbsent(id, s -> new MemoryBank());
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
    public void appendOptionsToSettings(MemoryBank memoryBank, OptionGroup.Builder builder) {}

    @Override
    public Collection<String> getAllIds() {
        return storage.keySet();
    }
}
