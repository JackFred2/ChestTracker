package red.jackf.chesttracker.storage;

import dev.isxander.yacl3.api.OptionGroup;
import red.jackf.chesttracker.memory.ItemMemory;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Handles saving/loading for memory
 */
public interface Storage {
    ItemMemory load(String worldId);

    void save(ItemMemory memory);

    void appendOptionsToSettings(ItemMemory memory, OptionGroup.Builder builder);

    Collection<String> getAllIds();

    enum Backend {
        JSON(JsonStorage::new),
        MEMORY(MemoryStorage::new);

        private final Supplier<Storage> constructor;

        Backend(Supplier<Storage> constructor) {
            this.constructor = constructor;
        }

        public void load() {
            var id = ItemMemory.INSTANCE != null ? ItemMemory.INSTANCE.getId() : null;
            ItemMemory.unload();
            StorageUtil.setStorage(constructor.get());
            if (id != null) ItemMemory.load(id);
        }
    }
}
