package red.jackf.chesttracker.memory;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    private Metadata metadata;
    private String id;

    public MemoryBank(Metadata metadata, Map<ResourceLocation, Map<BlockPos, Memory>> map) {
        this.metadata = metadata;
        this.memories = map;
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

    public Map<ResourceLocation, Map<BlockPos, Memory>> getMemories() {
        return memories;
    }

    public void addMemory(ResourceLocation key, BlockPos pos, Memory memory) {
        memories.computeIfAbsent(key, u -> new HashMap<>()).put(pos, memory);
    }

    public void removeMemory(ResourceLocation key, BlockPos pos) {
        if (memories.containsKey(key)) {
            memories.get(key).remove(pos);
        }
    }

    public void removeKey(ResourceLocation key) {
        memories.remove(key);
    }

    public Map<LightweightStack, Integer> getCounts(ResourceLocation key) {
        if (memories.containsKey(key))
            return memories.get(key).values().stream()
                    .flatMap(data -> data.items().stream())
                    .collect(Collectors.toMap(stack -> new LightweightStack(stack.getItem(),
                            stack.getTag()), ItemStack::getCount, Integer::sum, HashMap::new));
        else
            return Collections.emptyMap();
    }

    public List<SearchResult> getPositions(ResourceLocation key, SearchRequest request) {
        if (memories.containsKey(key))
            return memories.get(key).entrySet().stream()
                    .map(e -> Pair.of(e.getKey(), e.getValue().items().stream().filter(request::test).findFirst()))
                    .filter(pair -> pair.getSecond().isPresent())
                    .map(pair -> new SearchResult(pair.getFirst(), pair.getSecond().get()))
                    .collect(Collectors.toList());
        else
            return Collections.emptyList();
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
