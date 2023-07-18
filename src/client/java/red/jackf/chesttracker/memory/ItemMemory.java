package red.jackf.chesttracker.memory;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.storage.StorageUtil;
import red.jackf.whereisit.api.SearchRequest;

import java.util.*;
import java.util.stream.Collectors;

public class ItemMemory {
    private final Map<ResourceLocation, Map<BlockPos, LocationData>> memories;

    @Nullable
    public static ItemMemory INSTANCE = null;
    private String id;

    public static void load(String id) {
        if (INSTANCE != null) unload();
        INSTANCE = StorageUtil.getStorage().load(id);
        INSTANCE.id = id;
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

    public ItemMemory() {
        this(new HashMap<>());
    }

    public ItemMemory(Map<ResourceLocation, Map<BlockPos, LocationData>> map) {
        this.memories = map;
    }

    public Map<ResourceLocation, Map<BlockPos, LocationData>> getMemories() {
        return memories;
    }

    public void addMemory(ResourceLocation key, BlockPos pos, LocationData data) {
        memories.computeIfAbsent(key, u -> new HashMap<>()).put(pos, data);
    }

    public void removeMemory(ResourceLocation key, BlockPos pos) {
        if (memories.containsKey(key)) {
            memories.get(key).remove(pos);
        }
    }

    public Set<ResourceLocation> getKeys() {
        return memories.keySet();
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

    public List<BlockPos> getPositions(ResourceLocation key, SearchRequest request) {
        if (memories.containsKey(key))
            return memories.get(key).entrySet().stream()
                    .filter(e -> e.getValue().items().stream().anyMatch(request::test))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        else
            return Collections.emptyList();
    }

    public String getId() {
        return id;
    }
}
