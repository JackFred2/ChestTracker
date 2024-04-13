package red.jackf.chesttracker.impl.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.api.memory.Memory;
import red.jackf.chesttracker.api.memory.MemoryKey;
import red.jackf.chesttracker.api.memory.counting.CountingPredicate;
import red.jackf.chesttracker.api.memory.counting.StackMergeMode;
import red.jackf.chesttracker.impl.memory.key.SearchContext;
import red.jackf.chesttracker.impl.util.ItemStacks;
import red.jackf.chesttracker.impl.util.Misc;
import red.jackf.chesttracker.impl.util.ModCodecs;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;

import java.util.*;
import java.util.function.Predicate;

public class MemoryKeyImpl implements MemoryKey {
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
    private MemoryBankImpl memoryBank = null;

    public MemoryKeyImpl(Map<BlockPos, Memory> memories, Set<BlockPos> ignored) {
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

    public MemoryKeyImpl() {}

    protected void setMemoryBank(MemoryBankImpl bank) {
        this.memoryBank = bank;
    }

    public boolean isEmpty() {
        return this.memories.isEmpty();
    }

    public Map<BlockPos, Memory> getMemories() {
        return this.memories;
    }

    public Map<BlockPos, Memory> getNamedMemories() {
        return this.namedMemories;
    }

    public Set<BlockPos> ignored() {
        return this.ignored;
    }

    @Override
    public void add(BlockPos position, Memory memory) {
        // if no name and we require names, remove instead
        if (this.memoryBank.getMetadata().getFilteringSettings().onlyRememberNamed && memory.name() == null) {
            remove(position);
            return;
        }

        // if empty and no name (or we don't care about names), remove instead
        if (memory.isEmpty() && (memory.name() == null || !this.memoryBank.getMetadata().getIntegritySettings().preserveNamed)) {
            remove(position);
            return;
        }

        // TODO add context for gametime
        memory.touch(this.memoryBank.getMetadata().getLoadedTime(), Minecraft.getInstance().level.getGameTime());

        memory.otherPositions().forEach(this::remove);

        this.memories.put(position, memory);
        if (memory.name() != null)
            this.namedMemories.put(position, memory);
        for (BlockPos otherPosition : memory.otherPositions())
            this.connected.put(otherPosition, position);
    }

    @Override
    public boolean remove(BlockPos position) {
        BlockPos rootPosition = this.connected.getOrDefault(position, position);
        boolean success = this.memories.remove(rootPosition) != null;
        this.namedMemories.remove(rootPosition);
        //noinspection StatementWithEmptyBody
        while (this.connected.values().remove(rootPosition));
        return success;
    }

    public Optional<Memory> get(BlockPos pos) {
        return Optional.ofNullable(this.memories.get(pos));
    }

    @Override
    public List<ItemStack> getCounts(CountingPredicate predicate, StackMergeMode stackMergeMode) {
        return switch (stackMergeMode) {
            case ALL -> ItemStacks.flattenStacks(this.memories.entrySet().stream()
                    .filter(predicate)
                    .flatMap(data -> data.getValue().items().stream())
                    .toList(), false);
            case WITHIN_CONTAINERS -> this.memories.entrySet().stream()
                    .filter(predicate)
                    .flatMap(data -> ItemStacks.flattenStacks(data.getValue().items(), false).stream())
                    .toList();
            case NEVER -> this.memories.entrySet().stream()
                    .filter(predicate)
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
                        Misc.getAverageOffsetFrom(entry.getKey(), entry.getValue().otherPositions()).add(0, 1, 0)
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
        private static final Codec<MemoryKeyImpl> V2_3_3 = MEMORY_MAP.xmap(map -> new MemoryKeyImpl(map, Collections.emptySet()), MemoryKeyImpl::getMemories);

        // v2.4.0 and up
        // moved to record; adds blocked set
        private static final Codec<MemoryKeyImpl> LATEST = RecordCodecBuilder.create(
                instance -> instance.group(
                        MEMORY_MAP.fieldOf("memories").forGetter(MemoryKeyImpl::getMemories),
                        ModCodecs.set(ModCodecs.BLOCK_POS_STRING).fieldOf("blocked").forGetter(MemoryKeyImpl::ignored)
                ).apply(instance, MemoryKeyImpl::new)
        );

        public static final Codec<MemoryKeyImpl> MAIN = JFLCodecs.firstInList(LATEST, V2_3_3);
    }
}
