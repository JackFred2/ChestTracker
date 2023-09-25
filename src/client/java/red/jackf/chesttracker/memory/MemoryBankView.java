package red.jackf.chesttracker.memory;

import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.memory.metadata.Metadata;
import red.jackf.chesttracker.storage.Storage;

import java.util.ArrayList;
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
    void save();

    static MemoryBankView of(MemoryBank bank) {
        return new MemoryBankView() {
            private final Metadata copy = bank.getMetadata().deepCopy();
            private final List<ResourceLocation> toRemove = new ArrayList<>();

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
                return copy.getKeyOrder();
            }

            @Override
            public void removeKey(ResourceLocation id) {
                toRemove.add(id);
                copy.removeIcon(id);
            }

            public void save() {
                for (ResourceLocation key : toRemove)
                    bank.removeKey(key);
                bank.setMetadata(copy);
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

            public void save() {}
        };
    }
}
