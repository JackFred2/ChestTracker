package red.jackf.chesttracker.memory;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.SearchRequest;

import java.util.*;
import java.util.stream.Collectors;

public class ItemMemory {
    public static final ItemMemory INSTANCE = new ItemMemory();
    private final Map<ResourceLocation, Map<BlockPos, List<ItemStack>>> memories = new HashMap<>();

    public void addMemory(ResourceLocation key, BlockPos pos, List<ItemStack> items) {
        memories.computeIfAbsent(key, u -> new HashMap<>()).put(pos, items);
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
                    .flatMap(List::stream)
                    .collect(Collectors.toMap(stack -> new LightweightStack(stack.getItem(),
                            stack.getTag()), ItemStack::getCount, Integer::sum, HashMap::new));
        else
            return Collections.emptyMap();
    }

    public List<BlockPos> getPositions(ResourceLocation key, SearchRequest request) {
        if (memories.containsKey(key))
            return memories.get(key).entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(request::test))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        else
            return Collections.emptyList();
    }

}
