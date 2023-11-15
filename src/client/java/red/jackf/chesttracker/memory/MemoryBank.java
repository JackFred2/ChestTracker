package red.jackf.chesttracker.memory;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.api.provider.MemoryBuilder;
import red.jackf.chesttracker.memory.metadata.Metadata;
import red.jackf.chesttracker.provider.ProviderHandler;
import red.jackf.chesttracker.storage.ConnectionSettings;
import red.jackf.chesttracker.storage.Storage;
import red.jackf.chesttracker.util.CachedClientBlockSource;
import red.jackf.chesttracker.util.MemoryUtil;
import red.jackf.chesttracker.util.ModCodecs;
import red.jackf.jackfredlib.client.api.gps.Coordinate;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;

import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MemoryBank {
    public static final Codec<Map<ResourceLocation, Map<BlockPos, Memory>>> MEMORIES_CODEC = ModCodecs.makeMutableMap(
            Codec.unboundedMap(
                    ResourceLocation.CODEC,
                    ModCodecs.makeMutableMap(Codec.unboundedMap(
                            ModCodecs.BLOCK_POS_STRING,
                            Memory.CODEC
                    ))
            ));

    public static final ResourceLocation ENDER_CHEST_KEY = ChestTracker.id("ender_chest");

    @Nullable
    public static MemoryBank INSTANCE = null;

    /**
     * Automatically get and load a default memory based on the current context and connection-specific settings
     */
    public static void loadDefault(Coordinate coordinate) {
        // not in-game; don't load
        var settings = ConnectionSettings.getOrCreate(coordinate.id());
        var id = settings.memoryBankIdOverride().orElse(coordinate.id());
        loadOrCreate(id, Metadata.blankWithName(coordinate.userFriendlyName()));
    }

    public static void loadOrCreate(String id, @NotNull Metadata creationMetadata) {
        unload();
        INSTANCE = Storage.load(id).orElseGet(() -> {
            var bank = new MemoryBank(creationMetadata, new HashMap<>());
            bank.setId(id);
            return bank;
        });
        save();
    }

    public static void save() {
        if (INSTANCE == null) return;
        Storage.save(INSTANCE);
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

    // map of proxy positions to positions in the above map, changes lookup to O(1) i think
    private final Map<ResourceLocation, Map<BlockPos, BlockPos>> linkedPositions = new HashMap<>();

    // copy of memories with only named ones present for faster rendering iteration
    private final Map<ResourceLocation, Map<BlockPos, Memory>> namedMemories = new HashMap<>();
    private Metadata metadata;
    private String id;

    public MemoryBank(Metadata metadata, Map<ResourceLocation, Map<BlockPos, Memory>> map) {
        this.metadata = metadata;
        this.memories = map;

        for (var entry : memories.entrySet()) {
            for (var memory : entry.getValue().entrySet()) {
                // load named memory cache
                if (memory.getValue().name() != null)
                    this.namedMemories.computeIfAbsent(entry.getKey(), k -> new HashMap<>())
                                      .put(memory.getKey(), memory.getValue());

                // load linked positions
                addLinked(entry.getKey(), memory.getKey(), memory.getValue());
            }
        }
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
    public Map<ResourceLocation, Map<BlockPos, Memory>> getMemories() {
        return memories;
    }

    /**
     * Returns a specific memory key from this bank, or null if non-existent
     *
     * @param key Memory key to lookup
     * @return Memories for the given key, or null if non-existent
     */
    @Nullable
    public Map<BlockPos, Memory> getMemories(ResourceLocation key) {
        return memories.get(key);
    }

    /**
     * Returns a specific memory key from this bank, or null if non-existent. Only returns memories with names
     *
     * @param key Memory key to lookup
     * @return Memories with names for the given key, or null if non-existent
     */
    @Nullable
    public Map<BlockPos, Memory> getNamedMemories(ResourceLocation key) {
        return namedMemories.get(key);
    }

    /**
     * Add a memory to a specific memory builder entry.
     *
     * @param entry MemoryBuilder Entry containing data about the memory.
     */
    public void addMemory(MemoryBuilder.Entry entry) {
        if (Minecraft.getInstance().level == null) return;
        Memory memory = entry.memory().build(
                this.getMetadata().getLoadedTime(),
                Minecraft.getInstance().level.getGameTime(),
                Instant.now());
        if (this.getMetadata().getFilteringSettings().onlyRememberNamed && memory.name() == null) return;

        ResourceLocation key = entry.key();
        BlockPos pos = entry.position();

        for (BlockPos otherPos : memory.otherPositions())
            removeMemory(key, otherPos);

        _addMemory(memories, key, pos, memory);
        if (memory.name() != null) _addMemory(namedMemories, key, pos, memory);
        addLinked(key, pos, memory);
    }

    private static void _addMemory(
            Map<ResourceLocation, Map<BlockPos, Memory>> map,
            ResourceLocation key,
            BlockPos pos,
            Memory memory) {
        var keyMemories = map.get(key);
        if (memory.isEmpty() && keyMemories != null && memory.name() == null) {
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

    private void addLinked(ResourceLocation key, BlockPos pos, Memory memory) {
        if (!memory.otherPositions().isEmpty()) {
            var keyMap = this.linkedPositions.computeIfAbsent(key, k -> new HashMap<>());
            memory.otherPositions().forEach(linkedPos -> keyMap.put(linkedPos, pos));
        }
    }

    /**
     * Remove a memory from a given position and memory key, if one exists.
     *
     * @param key Memory key to check and remove
     * @param pos Position to remove in said key
     */
    public void removeMemory(ResourceLocation key, BlockPos pos) {
        if (linkedPositions.getOrDefault(key, Collections.emptyMap()).containsKey(pos))
            pos = linkedPositions.get(key).get(pos);
        _removeMemory(memories, key, pos);
        _removeMemory(namedMemories, key, pos);
        //noinspection StatementWithEmptyBody
        while (linkedPositions.getOrDefault(key, Collections.emptyMap()).values().remove(pos)) ;
    }

    private static void _removeMemory(
            Map<ResourceLocation, Map<BlockPos, Memory>> map,
            ResourceLocation key,
            BlockPos pos) {
        if (map.containsKey(key)) {
            map.get(key).remove(pos);
            if (map.get(key).isEmpty()) {
                map.remove(key);
            }
        }
    }

    /**
     * Remove an entire key from the current memory bank
     *
     * @param key Key to remove
     */
    public void removeKey(ResourceLocation key) {
        memories.remove(key);
        namedMemories.remove(key);
        linkedPositions.remove(key);
    }

    /**
     * Returns a list of counts of items in a given key; used in the main screen. Not sorted in any particular order.
     *
     * @param key    Memory Key to count and return
     * @param filter Filter to apply to memory entries before getting counted.
     * @return Arbitrary order list of all items in a given memory key.
     */
    public Map<LightweightStack, Integer> getCounts(
            ResourceLocation key,
            Predicate<Map.Entry<BlockPos, Memory>> filter) {
        if (memories.containsKey(key))
            return memories.get(key).entrySet().stream()
                           .filter(filter)
                           .flatMap(data -> data.getValue().items().stream())
                           .collect(Collectors.toMap(stack -> new LightweightStack(stack.getItem(),
                                                                                   stack.getTag()), ItemStack::getCount, Integer::sum, HashMap::new));
        else
            return Collections.emptyMap();
    }

    /*
     * Returns a list of counts of items in a given key at most <code>maxDistance</code> blocks away; used in the main
     * screen. Not sorted in any particular order.
     *
     * @param key Memory Key to count and return
     * @return Arbitrary order list of all items in a given memory key.
     */
    /*public Map<LightweightStack, Integer> getCounts(ResourceLocation key, Vec3 origin, int maxDistance) {
        long maxSquare = (long) maxDistance * maxDistance;
        if (memories.containsKey(key))
            return memories.get(key).entrySet().stream()
                           .filter(entry -> entry.getKey().getCenter().distanceToSqr(origin) < maxSquare)
                           .flatMap(data -> data.getValue().items().stream())
                           .collect(Collectors.toMap(stack -> new LightweightStack(stack.getItem(),
                                                                                   stack.getTag()), ItemStack::getCount, Integer::sum, HashMap::new));
        else
            return Collections.emptyMap();
    }*/

    /**
     * Parse a Where Is It search-request and runs it through a given dimension's memories.
     *
     * @param key     Memory key to run the request through
     * @param request Search request to run on all memories
     * @return A list of search requests consisting of matching memories in this key.
     */
    public List<SearchResult> getPositions(ResourceLocation key, SearchRequest request) {
        if (memories.containsKey(key)) {
            var results = new ArrayList<SearchResult>();
            final Vec3 startPos = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.position() : null;
            if (startPos == null) return Collections.emptyList();
            final int range = metadata.getSearchSettings().searchRange;
            final double rangeSquared = range == Integer.MAX_VALUE ? Integer.MAX_VALUE : range * range;
            for (Map.Entry<BlockPos, Memory> entry : memories.get(key).entrySet()) {
                if (entry.getKey().distToCenterSqr(startPos) > rangeSquared) continue;
                var matchedItem = entry.getValue().items().stream().filter(item -> SearchRequest.check(item, request))
                                       .findFirst();
                if (matchedItem.isEmpty()) continue;
                var offset = MemoryUtil.getAverageNameOffset(entry.getKey(), entry.getValue().otherPositions());
                results.add(SearchResult.builder(entry.getKey())
                                        .item(matchedItem.get())
                                        .name(entry.getValue().name(), offset)
                                        .otherPositions(entry.getValue().otherPositions())
                                        .build());
            }
            return results;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns a list of all memory keys in this bank.
     */
    public Set<ResourceLocation> getKeys() {
        return memories.keySet();
    }

    /**
     * Utility method for getting the current Memory at a given position; based on the current Level.
     *
     * @return Memory at the given position and current level, or null if non-existent.
     */
    @Nullable
    public static Memory getMemoryAt(Level level, BlockPos targetPos) {
        if (ProviderHandler.INSTANCE == null) return null;
        if (MemoryBank.INSTANCE == null) return null;
        if (!(level instanceof ClientLevel clientLevel)) return null;

        BlockState state = clientLevel.getBlockState(targetPos);
        var blockSource = new CachedClientBlockSource(clientLevel, targetPos, state);

        var override = ProviderHandler.INSTANCE.getKeyOverride(blockSource);

        ResourceLocation key;
        BlockPos pos;
        if (override.isPresent()) {
            key = override.get().getFirst();
            pos = override.get().getSecond();
        } else {
            key = ProviderHandler.getCurrentKey();
            pos = ConnectedBlocksGrabber.getConnected(clientLevel, state, targetPos).get(0);
        }

        if (key == null || pos == null) return null;

        var memoryKeys = MemoryBank.INSTANCE.getMemories(key);
        if (memoryKeys == null) return null;
        return memoryKeys.get(pos);
    }
}
