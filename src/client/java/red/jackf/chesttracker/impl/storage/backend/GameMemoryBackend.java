package red.jackf.chesttracker.impl.storage.backend;

import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.impl.memory.MemoryBankImpl;
import red.jackf.chesttracker.impl.memory.metadata.Metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GameMemoryBackend implements Backend {
    private static final Map<String, MemoryBankImpl> storage = new HashMap<>();

    @Nullable
    @Override
    public MemoryBankImpl load(String id) {
        return storage.get(id);
    }

    @Override
    public void delete(String id) {
        storage.remove(id);
    }

    @Override
    public boolean save(MemoryBankImpl memoryBank) {
        storage.put(memoryBank.getId(), memoryBank);
        return true;
    }

    @Override
    public Collection<String> getAllIds() {
        return storage.keySet();
    }

    @Override
    public Optional<Metadata> loadMetadata(String id) {
        return Optional.ofNullable(storage.get(id)).map(MemoryBankImpl::getMetadata);
    }

    @Override
    public boolean saveMetadata(String id, Metadata metadata) {
        var bank = storage.get(id);
        if (bank == null) {
            storage.put(id, new MemoryBankImpl(metadata, new HashMap<>()));
        } else {
            bank.setMetadata(metadata);
        }
        return true;
    }
}
