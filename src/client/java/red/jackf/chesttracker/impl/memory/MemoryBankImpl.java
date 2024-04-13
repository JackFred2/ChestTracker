package red.jackf.chesttracker.impl.memory;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.impl.ChestTracker;
import red.jackf.chesttracker.api.memory.Memory;
import red.jackf.chesttracker.api.memory.MemoryBank;
import red.jackf.chesttracker.api.memory.MemoryKey;
import red.jackf.chesttracker.api.memory.counting.CountingPredicate;
import red.jackf.chesttracker.api.memory.counting.StackMergeMode;
import red.jackf.chesttracker.impl.memory.key.SearchContext;
import red.jackf.chesttracker.impl.memory.metadata.Metadata;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;

import java.util.*;
import java.util.function.Predicate;

public class MemoryBankImpl implements MemoryBank {
    public static final Codec<Map<ResourceLocation, MemoryKeyImpl>> MEMORIES_CODEC = JFLCodecs.mutableMap(
            Codec.unboundedMap(
                    ResourceLocation.CODEC,
                    MemoryKeyImpl.Codecs.MAIN
            ));

    public static final ResourceLocation ENDER_CHEST_KEY = ChestTracker.id("ender_chest");

    ////////////
    // OBJECT //
    ////////////

    private final Map<ResourceLocation, MemoryKeyImpl> memoryKeys;
    private Metadata metadata;
    private String id;

    public MemoryBankImpl(Metadata metadata, Map<ResourceLocation, MemoryKeyImpl> keys) {
        this.metadata = metadata;
        this.memoryKeys = keys;
        this.memoryKeys.values().forEach(key -> key.setMemoryBank(this));
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    ///////////////////////
    // MEMORY MANAGEMENT //
    ///////////////////////

    /**
     * @return All memories in every key of this bank
     */
    public Map<ResourceLocation, MemoryKeyImpl> getMemories() {
        return memoryKeys;
    }

    /**
     * Returns a specific memory key from this bank, or null if non-existent
     *
     * @param key Memory key to lookup
     * @return Memories for the given key, or null if non-existent
     */
    @Nullable
    public MemoryKeyImpl getMemories(ResourceLocation key) {
        return memoryKeys.get(key);
    }

    /**
     * Remove an entire key from the current memory bank
     *
     * @param key Key to remove
     */
    public void removeKey(ResourceLocation key) {
        this.memoryKeys.remove(key);
    }

    /**
     * Returns a list of ItemStacks within a given key matching the given filter. Not sorted in a guaranteed order.
     *
     * @param key Memory key to pull from
     * @param filter Filter that memories must pass to be counted
     * @param stackMergeMode How to merge identical stacks
     */
    public List<ItemStack> getCounts(
            ResourceLocation key,
            CountingPredicate filter,
            StackMergeMode stackMergeMode) {
        if (this.memoryKeys.containsKey(key)) {
            return this.memoryKeys.get(key).getCounts(filter, stackMergeMode);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Parse a Where Is It search-request and runs it through a given dimension's memories.
     *
     * @param key     Memory key to run the request through
     * @param request Search request to run on all memories
     * @return A list of search requests consisting of matching memories in this key.
     */
    public List<SearchResult> doSearch(ResourceLocation key, SearchRequest request) {
        if (!this.memoryKeys.containsKey(key)) return Collections.emptyList();

        MemoryKeyImpl memoryKey = this.memoryKeys.get(key);
        final Vec3 startPos = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.position() : null;
        if (startPos == null) return Collections.emptyList();

        return memoryKey.doSearch(new SearchContext(
                request,
                startPos,
                this.metadata
        ));
    }

    /**
     * Returns a list of all memory keys in this bank.
     */
    public Set<ResourceLocation> getKeys() {
        return this.memoryKeys.keySet();
    }

    @Override
    public Set<ResourceLocation> getMemoryKeys() {
        return Set.copyOf(this.memoryKeys.keySet());
    }

    @Override
    public Map<ResourceLocation, MemoryKey> getAllMemories() {
        return Map.copyOf(this.memoryKeys);
    }

    @Override
    public Optional<MemoryKey> getKey(ResourceLocation keyId) {
        return Optional.ofNullable(this.memoryKeys.get(keyId));
    }

    @Override
    public void addMemory(ResourceLocation keyId, BlockPos location, Memory memory) {
        MemoryKeyImpl key = this.memoryKeys.computeIfAbsent(keyId, ignored -> {
            var newKey = new MemoryKeyImpl();
            newKey.setMemoryBank(this);
            return newKey;
        });

        key.add(location, memory);

        // if we didn't want the memory
        if (key.isEmpty()) {
            this.memoryKeys.remove(keyId);
        }
    }

    @Override
    public void removeMemory(ResourceLocation key, BlockPos pos) {
        MemoryKeyImpl memoryKey = this.memoryKeys.get(key);
        if (memoryKey != null) {
            memoryKey.remove(pos);
            if (memoryKey.isEmpty()) {
                this.memoryKeys.remove(key);
            }
        }
    }

}
