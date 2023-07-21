package red.jackf.chesttracker.memory;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.storage.LoadContext;
import red.jackf.chesttracker.storage.StorageUtil;
import red.jackf.chesttracker.util.ModCodec;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;

import java.util.*;
import java.util.stream.Collectors;

public class MemoryBank {
    private static final Codec<Map<ResourceLocation, Map<BlockPos, Memory>>> MEMORY_CODEC = ModCodec.makeMutableMap(
            Codec.unboundedMap(
                ResourceLocation.CODEC,
                ModCodec.makeMutableMap(Codec.unboundedMap(
                        ModCodec.BLOCK_POS_STRING,
                        Memory.CODEC
                ))
            ));

    public static final Codec<MemoryBank> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    MEMORY_CODEC.fieldOf("memories").forGetter(MemoryBank::getMemories),
                    Codec.STRING.optionalFieldOf("name").forGetter(bank -> Optional.ofNullable(bank.getName()))
            ).apply(instance, (memories, name) -> new MemoryBank(memories, name.orElse(null))));
    private final Map<ResourceLocation, Map<BlockPos, Memory>> memories;
    @Nullable
    private String name;

    @Nullable
    public static MemoryBank INSTANCE = null;
    private String id;

    private void setId(String id) {
        this.id = id;
    }

    public static void loadOrCreate(String id, @Nullable LoadContext ctx) {
        unload();
        INSTANCE = StorageUtil.getStorage().loadOrCreate(id, () -> {
            if (ctx == null) return new MemoryBank(new HashMap<>(), null);
            return new MemoryBank(new HashMap<>(), ctx.name());
        });
        INSTANCE.setId(id);
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

    private MemoryBank(Map<ResourceLocation, Map<BlockPos, Memory>> map, @Nullable String name) {
        this.memories = map;
        this.name = name;
    }

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

    public String getId() {
        return id;
    }

    public Set<ResourceLocation> getKeys() {
        return memories.keySet();
    }

    public String getName() {
        return name == null ? id : name;
    }
}
