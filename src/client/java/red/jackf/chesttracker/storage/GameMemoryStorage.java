package red.jackf.chesttracker.storage;

import dev.isxander.yacl3.api.OptionGroup;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.memory.MemoryBank;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GameMemoryStorage implements Storage {
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
    public void appendOptionsToSettings(MemoryBank memoryBank, OptionGroup.Builder builder) {}

    @Override
    public Collection<String> getAllIds() {
        return storage.keySet();
    }
}
