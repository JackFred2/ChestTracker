package red.jackf.chesttracker.memory;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import red.jackf.whereisit.api.SearchRequest;

import java.util.*;
import java.util.stream.Collectors;

public class ItemMemory {
    public static final ItemMemory INSTANCE = new ItemMemory();
    private final Map<ResourceKey<Level>, Map<BlockPos, List<ItemStack>>> memories = new HashMap<>();

    public void addMemory(ResourceKey<Level> level, BlockPos pos, List<ItemStack> items) {
        memories.computeIfAbsent(level, key -> new HashMap<>()).put(pos, items);
    }

    public void removeMemory(ResourceKey<Level> level, BlockPos pos) {
        if (memories.containsKey(level)) {
            memories.get(level).remove(pos);
        }
    }

    public Set<ResourceKey<Level>> getLevels() {
        return memories.keySet();
    }

    public Map<LightweightStack, Integer> getCounts(ResourceKey<Level> level) {
        if (memories.containsKey(level))
            return memories.get(level).values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toMap(stack -> new LightweightStack(stack.getItem(),
                            stack.getTag()), ItemStack::getCount, Integer::sum, HashMap::new));
        else
            return Collections.emptyMap();
    }

    public List<BlockPos> getPositions(ResourceKey<Level> level, SearchRequest request) {
        if (memories.containsKey(level))
            return memories.get(level).entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(request::test))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        else
            return Collections.emptyList();
    }

}
