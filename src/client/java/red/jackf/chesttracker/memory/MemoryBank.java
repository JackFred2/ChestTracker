package red.jackf.chesttracker.memory;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.MemoryKeyIcon;
import red.jackf.chesttracker.storage.LoadContext;
import red.jackf.chesttracker.storage.StorageUtil;
import red.jackf.chesttracker.util.ModCodecs;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class MemoryBank {
    private static final Codec<Map<ResourceLocation, Map<BlockPos, Memory>>> MEMORY_CODEC = ModCodecs.makeMutableMap(
            Codec.unboundedMap(
                ResourceLocation.CODEC,
                ModCodecs.makeMutableMap(Codec.unboundedMap(
                        ModCodecs.BLOCK_POS_STRING,
                        Memory.CODEC
                ))
            ));

    public static final Codec<MemoryBank> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Metadata.CODEC.fieldOf("metadata").forGetter(MemoryBank::getMetadata),
                    MEMORY_CODEC.fieldOf("memories").forGetter(MemoryBank::getMemories)
            ).apply(instance, MemoryBank::new));

    public static final ResourceLocation ENDER_CHEST_KEY = ChestTracker.id("ender_chest");

    @Nullable
    public static MemoryBank INSTANCE = null;

    public static void loadOrCreate(String id, @NotNull Metadata creationMetadata) {
        unload();
        INSTANCE = StorageUtil.getStorage().load(id);
        if (INSTANCE == null) {
            INSTANCE = new MemoryBank(creationMetadata, new HashMap<>());
        }
        INSTANCE.setId(id);
        save();
    }

    public static void save() {
        if (INSTANCE == null) return;
        StorageUtil.getStorage().save(INSTANCE);
    }

    public static void unload() {
        if (INSTANCE == null) return;
        save();
        INSTANCE = null;
    }

    ////////////
    // OBJECT //
    ////////////

    private final Map<ResourceLocation, Map<BlockPos, Memory>> memories;

    // copy of memories with only named ones present for faster rendering iteration
    private final Map<ResourceLocation, Map<BlockPos, Memory>> namedMemories = new HashMap<>();
    private Metadata metadata;
    private String id;

    public MemoryBank(Metadata metadata, Map<ResourceLocation, Map<BlockPos, Memory>> map) {
        this.metadata = metadata;
        this.memories = map;

        for (var entry : memories.entrySet())
            for (var memory : entry.getValue().entrySet())
                if (memory.getValue().name() != null)
                    this.namedMemories.computeIfAbsent(entry.getKey(), k -> new HashMap<>()).put(memory.getKey(), memory.getValue());
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
    private Map<ResourceLocation, Map<BlockPos, Memory>> getMemories() {
        return memories;
    }

    /**
     * Returns a specific memory key from this bank, or null if non-existent
     * @param key Memory key to lookup
     * @return Memories for the given key, or null if non-existent
     */
    @Nullable
    public Map<BlockPos, Memory> getMemories(ResourceLocation key) {
        return memories.get(key);
    }

    /**
     * Returns a specific memory key from this bank, or null if non-existent. Only returns memories with names
     * @param key Memory key to lookup
     * @return Memories with names for the given key, or null if non-existent
     */
    @Nullable
    public Map<BlockPos, Memory> getNamedMemories(ResourceLocation key) {
        return memories.get(key);
    }

    /**
     * Add a memory to a specific key and position
     * @param key Key to add the memory to; usually a dimension ID, or a custom location
     * @param pos Position to add the memory to
     * @param memory Memory to add
     */
    public void addMemory(ResourceLocation key, BlockPos pos, Memory memory) {
        _addMemory(memories, key, pos, memory);
        if (memory.name() != null) _addMemory(namedMemories, key, pos, memory);
    }

    private static void _addMemory(Map<ResourceLocation, Map<BlockPos, Memory>> map, ResourceLocation key, BlockPos pos, Memory memory) {
        var keyMemories = map.get(key);
        if (memory.isEmpty() && keyMemories != null) {
            keyMemories.remove(pos);
            if (keyMemories.isEmpty()) map.remove(key);
        } else {
            if (keyMemories == null) {
                keyMemories = new HashMap<>();
                map.put(key, keyMemories);
            }
            keyMemories.put(pos, memory);
        }
    }

    /**
     * Remove a memory from a given position and memory key, if one exists.
     * @param key Memory key to check and remove
     * @param pos Position to remove in said key
     */
    public void removeMemory(ResourceLocation key, BlockPos pos) {
        _removeMemory(memories, key, pos);
        _removeMemory(namedMemories, key, pos);
    }

    private static void _removeMemory(Map<ResourceLocation, Map<BlockPos, Memory>> map, ResourceLocation key, BlockPos pos) {
        if (map.containsKey(key)) {
            map.get(key).remove(pos);
            if (map.get(key).isEmpty()) {
                map.remove(key);
            }
        }
    }

    /**
     * Remove an entire key from the current memory bank
     * @param key Key to remove
     */
    public void removeKey(ResourceLocation key) {
        memories.remove(key);
        namedMemories.remove(key);
    }

    /**
     * Returns a list of counts of items in a given key; used in the main screen. Not sorted in any particular order.
     * @param key Memory Key to count and return
     * @return Arbitrary order list of all items in a given memory key.
     */
    public Map<LightweightStack, Integer> getCounts(ResourceLocation key) {
        if (memories.containsKey(key))
            return memories.get(key).values().stream()
                    .flatMap(data -> data.items().stream())
                    .collect(Collectors.toMap(stack -> new LightweightStack(stack.getItem(),
                            stack.getTag()), ItemStack::getCount, Integer::sum, HashMap::new));
        else
            return Collections.emptyMap();
    }

    /**
     * Parse a Where Is It search-request and runs it through a given dimension's memories.
     * @param key Memory key to run the request through
     * @param request Search request to run on all memories
     * @return A list of search requests consisting of matching memories in this key.
     */
    public List<SearchResult> getPositions(ResourceLocation key, SearchRequest request) {
        if (memories.containsKey(key)) {
            var results = new ArrayList<SearchResult>();
            for (Map.Entry<BlockPos, Memory> entry : memories.get(key).entrySet()) {
                var matchedItem = entry.getValue().items().stream().filter(item -> SearchRequest.check(item, request)).findFirst();
                if (matchedItem.isEmpty()) continue;
                results.add(SearchResult.builder(entry.getKey())
                        .item(matchedItem.get())
                        .name(entry.getValue().name(), null)
                        .build());
            }
            return results;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns a list of all memory keys in this bank, in order of the list in the config, then an arbitrary order.
     */
    public List<ResourceLocation> getKeys() {
        var keys = memories.keySet();
        List<ResourceLocation> sorted = new ArrayList<>(keys.size());
        for (MemoryKeyIcon icon : ChestTrackerConfig.INSTANCE.getConfig().gui.memoryKeyIcons)
            if (keys.contains(icon.id()))
                sorted.add(icon.id());
        for (var key : keys)
            if (!sorted.contains(key))
                sorted.add(key);
        return sorted;
    }

    public static class Metadata {
        public static final Codec<Metadata> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("name").forGetter(meta -> Optional.ofNullable(meta.name)),
                    ModCodecs.INSTANT.fieldOf("lastModified").forGetter(meta -> meta.lastModified)
            ).apply(instance, (name, modified) -> new Metadata(name.orElse(null), modified))
        );

        @Nullable
        private String name;
        private Instant lastModified;

        public Metadata(@Nullable String name, Instant lastModified) {
            this.name = name;
            this.lastModified = lastModified;
        }

        public static Metadata blank() {
            return new Metadata(null, Instant.now());
        }

        public static Metadata from(LoadContext ctx) {
            return new Metadata(ctx.name(), Instant.now());
        }

        @Nullable
        public String getName() {
            return name;
        }

        public void setName(@Nullable String name) {
            this.name = name;
        }

        public Instant getLastModified() {
            return lastModified;
        }

        public void updateModified() {
            this.lastModified = Instant.now();
        }
    }
}
