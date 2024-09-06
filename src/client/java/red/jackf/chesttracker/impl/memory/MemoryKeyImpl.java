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
import red.jackf.chesttracker.impl.datafix.Types;
import red.jackf.chesttracker.impl.memory.key.ManualMode;
import red.jackf.chesttracker.impl.memory.key.OverrideInfo;
import red.jackf.chesttracker.impl.memory.key.SearchContext;
import red.jackf.chesttracker.impl.util.ItemStacks;
import red.jackf.chesttracker.impl.util.Misc;
import red.jackf.chesttracker.impl.util.ModCodecs;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.api.search.NestedItemsGrabber;

import java.util.*;
import java.util.stream.Stream;

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

    private final Map<BlockPos, OverrideInfo> overrides = new HashMap<>();
    private MemoryBankImpl memoryBank = null;

    public MemoryKeyImpl(Map<BlockPos, Memory> memories, Map<BlockPos, OverrideInfo> overrides) {
        this.memories.putAll(memories);

        this.overrides.putAll(overrides);

        for (Map.Entry<BlockPos, Memory> entry : memories.entrySet()) {
            BlockPos pos = entry.getKey();
            Memory memory = entry.getValue();
            memory.populate(this, entry.getKey());
            if (memory.hasCustomName())
                this.namedMemories.put(entry.getKey(), memory);
            for (BlockPos otherPosition : memory.otherPositions())
                this.connected.put(otherPosition, entry.getKey());
        }
    }

    public MemoryKeyImpl() {}

    protected void setMemoryBank(MemoryBankImpl bank) {
        this.memoryBank = bank;
    }

    public MemoryBankImpl getMemoryBank() {
        return memoryBank;
    }

    public boolean isEmpty() {
        return this.memories.isEmpty() && this.overrides.isEmpty();
    }

    public Map<BlockPos, Memory> getMemories() {
        return this.memories;
    }

    public Map<BlockPos, Memory> getNamedMemories() {
        return this.namedMemories;
    }

    public Map<BlockPos, OverrideInfo> overrides() {
        return this.overrides;
    }

    public void add(BlockPos position, Memory memory) {
        // if blocked remove instead
        OverrideInfo override = this.overrides.get(position);
        ManualMode manualMode = override != null ? override.getManualMode() : ManualMode.DEFAULT;
        boolean shouldAdd = manualMode == ManualMode.REMEMBER // force remember
                || manualMode == ManualMode.DEFAULT && !this.memoryBank.getMetadata().getFilteringSettings().manualMode // no override but default is remember
                || this.memories.containsKey(position); // already a memory
        if (!shouldAdd) {
            return;
        }

        memory.populate(this, position);

        // if no name and we require names, remove instead
        if (this.memoryBank.getMetadata().getFilteringSettings().onlyRememberNamed && !memory.hasCustomName()) {
            remove(position);
            return;
        }

        // if empty and no name (or we don't care about names), remove instead
        if (memory.isEmpty() && (!memory.hasCustomName() || !this.memoryBank.getMetadata().getIntegritySettings().preserveNamed)) {
            remove(position);
            return;
        }

        // TODO add context for gametime
        memory.touch(this.memoryBank.getMetadata().getLoadedTime(), Minecraft.getInstance().level.getGameTime());

        // Shuffle along an override from an existing position thats now connected to the original
        OverrideInfo existingOverride = null;
        for (BlockPos blockPos : memory.otherPositions()) {
            if (this.overrides.containsKey(blockPos))
                existingOverride = this.overrides.get(blockPos);
            remove(blockPos);
        }

        if (existingOverride != null)
            this.overrides.put(position, existingOverride);

        this.memories.put(position, memory);
        if (memory.hasCustomName())
            this.namedMemories.put(position, memory);
        for (BlockPos otherPosition : memory.otherPositions())
            this.connected.put(otherPosition, position);
    }

    public boolean remove(BlockPos position) {
        BlockPos rootPosition = this.connected.getOrDefault(position, position);
        boolean success = this.memories.remove(rootPosition) != null;
        this.namedMemories.remove(rootPosition);
        //noinspection StatementWithEmptyBody
        while (this.connected.values().remove(rootPosition));

        this.overrides.remove(position);
        return success;
    }

    public Optional<Memory> get(BlockPos pos) {
        return Optional.ofNullable(this.memories.get(this.connected.getOrDefault(pos, pos)));
    }

    @Override
    public List<ItemStack> getCounts(CountingPredicate predicate, StackMergeMode stackMergeMode, boolean unpackNested) {
        List<List<ItemStack>> items = this.memories.entrySet().stream()
                .filter(predicate)
                .map(entry -> {
                    if (unpackNested) {
                        return entry.getValue().items().stream()
                                .flatMap(stack -> Stream.concat(Stream.of(stack), NestedItemsGrabber.get(stack)))
                                .toList();
                    } else {
                        return entry.getValue().items();
                    }
                }).toList();

        return switch (stackMergeMode) {
            case ALL -> ItemStacks.flattenStacks(items.stream().flatMap(Collection::stream).toList(), false);
            case WITHIN_CONTAINERS -> items.stream().flatMap(list -> ItemStacks.flattenStacks(list, false).stream()).toList();
            case NEVER -> items.stream().flatMap(Collection::stream).toList();
        };
    }

    public List<SearchResult> doSearch(SearchContext context) {
        List<SearchResult> results = new ArrayList<>();
        final long rangeSquared = (long) context.metadata().getSearchSettings().searchRange
                                * (long) context.metadata().getSearchSettings().searchRange;

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
                        entry.getValue().renderName(),
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
        private static final Codec<MemoryKeyImpl> V2_3_3 = Types.wrapInFixer(Types.MEMORY_DATA_2_3_3,
                MEMORY_MAP.xmap(map -> new MemoryKeyImpl(map, Collections.emptyMap()), MemoryKeyImpl::getMemories),
                3700); // Data Version 3700 -> Minecraft 1.20.4

        // v2.4.0 and up
        // moved to record; adds blocked set
        private static final Codec<MemoryKeyImpl> LATEST = ModCodecs.predicate( // we wrap in predicate because otherwise DFU dumps the whole bank into the console even though its fine
                dyn -> dyn.get("memories").result().isPresent(),
                Types.wrapInFixer(
                        Types.MEMORY_DATA,
                        RecordCodecBuilder.create(
                        instance -> instance.group(
                                MEMORY_MAP.fieldOf("memories").forGetter(MemoryKeyImpl::getMemories),
                                Codec.unboundedMap(ModCodecs.BLOCK_POS_STRING, OverrideInfo.CODEC).fieldOf("overrides").forGetter(MemoryKeyImpl::overrides)
                        ).apply(instance, MemoryKeyImpl::new)),
                        3700 // Data Version 3700 -> Minecraft 1.20.4
                )
        );

        public static final Codec<MemoryKeyImpl> MAIN = JFLCodecs.firstInList(LATEST, V2_3_3);
    }
}
