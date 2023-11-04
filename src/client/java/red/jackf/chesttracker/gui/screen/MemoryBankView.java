package red.jackf.chesttracker.gui.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.memory.Memory;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.memory.metadata.Metadata;
import red.jackf.chesttracker.storage.Storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * View of a memory bank for management purposes
 */
public interface MemoryBankView {
    String id();

    Metadata metadata();

    List<ResourceLocation> keys();

    @Nullable Map<BlockPos, Memory> getMemories(ResourceLocation memoryKey);

    void removeKey(ResourceLocation id);

    void remove(ResourceLocation id, BlockPos pos);

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
                return copy.getVisualSettings().getKeyOrder();
            }

            @Override
            public @Nullable Map<BlockPos, Memory> getMemories(ResourceLocation memoryKey) {
                return bank.getMemories(memoryKey);
            }

            @Override
            public void removeKey(ResourceLocation id) {
                toRemove.add(id);
                copy.getVisualSettings().removeIcon(id);
            }

            @Override
            public void remove(ResourceLocation id, BlockPos pos) {
                bank.removeMemory(id, pos);
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
            public @Nullable Map<BlockPos, Memory> getMemories(ResourceLocation memoryKey) {
                return null;
            }

            @Override
            public void removeKey(ResourceLocation id) {}

            @Override
            public void remove(ResourceLocation id, BlockPos pos) {}

            public void save() {}
        };
    }
}
