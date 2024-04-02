package red.jackf.chesttracker.memory;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.api.provider.MemoryBuildContext;
import red.jackf.chesttracker.api.provider.MemoryBuilder;
import red.jackf.chesttracker.memory.key.MemoryKey;
import red.jackf.chesttracker.memory.key.SearchContext;
import red.jackf.chesttracker.memory.metadata.Metadata;
import red.jackf.chesttracker.provider.ProviderHandler;
import red.jackf.chesttracker.storage.ConnectionSettings;
import red.jackf.chesttracker.storage.Storage;
import red.jackf.chesttracker.util.CachedClientBlockSource;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;
import red.jackf.jackfredlib.client.api.gps.Coordinate;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;

import java.util.*;
import java.util.function.Predicate;

public class MemoryBank {
    public static final Codec<Map<ResourceLocation, MemoryKey>> MEMORIES_CODEC = JFLCodecs.mutableMap(
            Codec.unboundedMap(
                    ResourceLocation.CODEC,
                    MemoryKey.Codecs.KEY
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

    private final Map<ResourceLocation, MemoryKey> memoryKeys;
    private Metadata metadata;
    private String id;

    public MemoryBank(Metadata metadata, Map<ResourceLocation, MemoryKey> keys) {
        this.metadata = metadata;
        this.memoryKeys = keys;
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
    public Map<ResourceLocation, MemoryKey> getMemories() {
        return memoryKeys;
    }

    /**
     * Returns a specific memory key from this bank, or null if non-existent
     *
     * @param key Memory key to lookup
     * @return Memories for the given key, or null if non-existent
     */
    @Nullable
    public MemoryKey getMemories(ResourceLocation key) {
        return memoryKeys.get(key);
    }

    /**
     * Add a memory to a specific memory builder entry.
     *
     * @param entry MemoryBuilder Entry containing data about the memory.
     */
    public void addMemory(MemoryBuilder.Entry entry) {
        if (Minecraft.getInstance().level == null) return;

        ResourceLocation key = entry.key();

        MemoryKey memoryKey = this.memoryKeys.computeIfAbsent(key, ignored -> new MemoryKey());

        memoryKey.addMemory(entry, new MemoryBuildContext(this.metadata, Minecraft.getInstance().level.getGameTime()));
        if (memoryKey.isEmpty()) {
            this.memoryKeys.remove(key);
        }
    }

    /**
     * Remove a memory from a given position and memory key, if one exists.
     *
     * @param key Memory key to check and remove
     * @param pos Position to remove in said key
     */
    public void removeMemory(ResourceLocation key, BlockPos pos) {
        MemoryKey memoryKey = this.memoryKeys.get(key);
        if (memoryKey != null) {
            memoryKey.removeMemory(pos);
            if (memoryKey.isEmpty()) {
                this.memoryKeys.remove(key);
            }
        }
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
            Predicate<Map.Entry<BlockPos, Memory>> filter,
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

        MemoryKey memoryKey = this.memoryKeys.get(key);
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

    public enum StackMergeMode {
        ALL(Component.translatable("chesttracker.gui.editMemoryBank.search.stackMergeMode.all")),
        WITHIN_CONTAINERS(Component.translatable("chesttracker.gui.editMemoryBank.search.stackMergeMode.withinContainers")),
        NEVER(Component.translatable("chesttracker.gui.editMemoryBank.search.stackMergeMode.never"));

        public final Component label;

        StackMergeMode(Component label) {
            this.label = label;
        }
    }
}
