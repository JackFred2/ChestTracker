package red.jackf.chesttracker.storage;

import dev.isxander.yacl3.api.OptionGroup;
import red.jackf.chesttracker.memory.MemoryBank;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Handles saving/loading for memoryBank
 */
public interface Storage {
    MemoryBank load(String id);
    void delete(String id);

    void save(MemoryBank memoryBank);

    void appendOptionsToSettings(MemoryBank memoryBank, OptionGroup.Builder builder);

    Collection<String> getAllIds();

    enum Backend {
        JSON(JsonStorage::new),
        MEMORY(MemoryStorage::new);

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
