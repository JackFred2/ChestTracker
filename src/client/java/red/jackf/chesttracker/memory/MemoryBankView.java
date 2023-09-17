package red.jackf.chesttracker.memory;

import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.storage.Storage;

import java.util.Collections;
import java.util.List;

/**
 * View of a memory bank for management purposes
 */
public interface MemoryBankView {
    String id();

    Metadata metadata();

    List<ResourceLocation> keys();

    void removeKey(ResourceLocation id);

    void apply();
    void save();

    static MemoryBankView of(MemoryBank bank) {
        return new MemoryBankView() {
            private final Metadata copy = bank.getMetadata().deepCopy();

            @Override
            public String id() {
                return bank.getId();
            }

            @Override
            public Metadata metadata() {
                return copy;
            }

            @Override
            public List<ResourceLocation> keys() {
                return bank.getKeys();
            }

            @Override
            public void removeKey(ResourceLocation id) {
                bank.removeKey(id);
            }

            @Override
            public void apply() {
                bank.setMetadata(copy);
            }

            public void save() {
                Storage.save(bank);
            }
        };
    }

    static MemoryBankView empty() {
        return new MemoryBankView() {
            @Override
            public String id() {
                return "error";
            }

            @Override
            public Metadata metadata() {
                return Metadata.blank();
            }

            @Override
            public List<ResourceLocation> keys() {
                return Collections.emptyList();
            }

            @Override
            public void removeKey(ResourceLocation id) {}

            @Override
            public void apply() {}

            public void save() {}
        };
    }
}
