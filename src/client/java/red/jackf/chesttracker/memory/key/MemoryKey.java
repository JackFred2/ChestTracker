package red.jackf.chesttracker.memory.key;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.provider.MemoryBuildContext;
import red.jackf.chesttracker.api.provider.MemoryBuilder;
import red.jackf.chesttracker.memory.Memory;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.util.ItemStackUtil;
import red.jackf.chesttracker.util.MemoryUtil;
import red.jackf.chesttracker.util.ModCodecs;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;

import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

public class MemoryKey {
    private final Map<BlockPos, Memory> memories = new HashMap<>();

    /**
     * Cache of all memories with names; used for faster rendering.
     */
    private final Map<BlockPos, Memory> namedMemories = new HashMap<>();

    /**
     * Cache map of which positions are linked together (think double chests).
     * Used for both memory lookup and blocklist
     */
    private final Map<BlockPos, BlockPos> connected = new HashMap<>();

    private final Set<BlockPos> ignored = new HashSet<>();

    public MemoryKey() {}

    public MemoryKey(Map<BlockPos, Memory> memories, Set<BlockPos> ignored) {
        this.memories.putAll(memories);

        for (Map.Entry<BlockPos, Memory> entry : memories.entrySet()) {
            Memory memory = entry.getValue();
            if (memory.name() != null)
                this.namedMemories.put(entry.getKey(), memory);
            for (BlockPos otherPosition : memory.otherPositions())
                this.connected.put(otherPosition, entry.getKey());
        }

        this.ignored.addAll(ignored);
    }

    public boolean isEmpty() {
        return this.memories.isEmpty();
    }

    public Map<BlockPos, Memory> memories() {
        return this.memories;
    }

    public Map<BlockPos, Memory> namedMemories() {
        return this.namedMemories;
    }

    public Set<BlockPos> ignored() {
        return this.ignored;
    }

    public void addMemory(MemoryBuilder.Entry entry, MemoryBuildContext context) {
        BlockPos pos = entry.position();
        Memory memory = entry.memory().build(
                context.metadata().getLoadedTime(),
                context.levelGameTime(),
                Instant.now());
        if (context.metadata().getFilteringSettings().onlyRememberNamed && memory.name() == null)
            return;

        // don't bother keeping empty memories, unless it has a name and we care about that
        if (memory.isEmpty() && (memory.name() == null || !context.metadata().getIntegritySettings().preserveNamed)) {
            removeMemory(pos);
            return;
        }

        for (BlockPos otherPosition : memory.otherPositions()) {
            removeMemory(otherPosition);
        }

        this.memories.put(pos, memory);
        if (memory.name() != null)
            this.namedMemories.put(pos, memory);
        for (BlockPos otherPosition : memory.otherPositions())
            this.connected.put(otherPosition, pos);
    }

    public void removeMemory(BlockPos position) {
        BlockPos rootPosition = this.connected.getOrDefault(position, position);
        this.memories.remove(rootPosition);
        this.namedMemories.remove(rootPosition);
        //noinspection StatementWithEmptyBody
        while (this.connected.values().remove(rootPosition));
    }

    @Nullable
    public Memory get(BlockPos pos) {
        return this.memories.get(pos);
    }

    public List<ItemStack> getCounts(Predicate<Map.Entry<BlockPos, Memory>> filter, MemoryBank.StackMergeMode mergeMode) {
        return switch (mergeMode) {
            case ALL -> ItemStackUtil.flattenStacks(this.memories.entrySet().stream()
                    .filter(filter)
                    .flatMap(data -> data.getValue().items().stream())
                    .toList(), false);
            case WITHIN_CONTAINERS -> this.memories.entrySet().stream()
                    .filter(filter)
                    .flatMap(data -> ItemStackUtil.flattenStacks(data.getValue().items(), false).stream())
                    .toList();
            case NEVER -> this.memories.entrySet().stream()
                    .filter(filter)
                    .flatMap(data -> data.getValue().items().stream())
                    .toList();
        };
    }

    public List<SearchResult> doSearch(SearchContext context) {
        List<SearchResult> results = new ArrayList<>();
        final int rangeSquared = context.metadata().getSearchSettings().searchRange
                * context.metadata().getSearchSettings().searchRange;

        for (Map.Entry<BlockPos, Memory> entry : this.memories.entrySet()) {
            if (entry.getKey().distToCenterSqr(context.rootPosition()) > rangeSquared) continue;
            Optional<ItemStack> matchingItem = entry.getValue().items().stream()
                    .filter(stack -> SearchRequest.check(stack, context.request()))
                    .findFirst();
            if (matchingItem.isEmpty()) continue;

            SearchResult.Builder result = SearchResult.builder(entry.getKey())
                    .item(matchingItem.get())
                    .otherPositions(entry.getValue().otherPositions());

            if (context.metadata().getCompatibilitySettings().displayContainerNames)
                result.name(
                        entry.getValue().name(),
                        MemoryUtil.getAverageNameOffset(entry.getKey(), entry.getValue().otherPositions())
                );

            results.add(result.build());
        }

        return results;
    }

    public static class Codecs {
        private static final Codec<Map<BlockPos, Memory>> MEMORY_MAP = Codec.unboundedMap(
                ModCodecs.BLOCK_POS_STRING,
                Memory.CODEC
        );

        // v2.3.3 and below
        // just a map of positions to memories
        private static final Codec<MemoryKey> V2_3_3 = MEMORY_MAP.xmap(map -> new MemoryKey(map, Collections.emptySet()), MemoryKey::memories);

        // v2.4.0 and up
        // moved to record; adds blocked set
        private static final Codec<MemoryKey> LATEST = RecordCodecBuilder.create(
                instance -> instance.group(
                        MEMORY_MAP.fieldOf("memories").forGetter(MemoryKey::memories),
                        ModCodecs.set(ModCodecs.BLOCK_POS_STRING).fieldOf("blocked").forGetter(MemoryKey::ignored)
                ).apply(instance, MemoryKey::new)
        );

        public static final Codec<MemoryKey> MAIN = JFLCodecs.firstInList(LATEST, V2_3_3);
    }
}
