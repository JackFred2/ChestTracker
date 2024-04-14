package red.jackf.chesttracker.api.memory;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.memory.counting.CountingPredicate;
import red.jackf.chesttracker.api.memory.counting.StackMergeMode;
import red.jackf.chesttracker.api.providers.ProviderUtils;
import red.jackf.chesttracker.impl.util.CachedClientBlockSource;

import java.util.*;

/**
 * Interface for working with a loaded memory bank.
 */
public interface MemoryBank {
    /**
     * Returns an immutable set of all memory keys with memories in this bank.
     *
     * @return Set of all keys with memories in this memory bank.
     */
    Set<ResourceLocation> getMemoryKeys();

    /**
     * Returns an immutable map of all memories in this memory bank.
     *
     * @return A map of resource locations to {@link MemoryKey}s, which contain memories.
     */
    Map<ResourceLocation, MemoryKey> getAllMemories();

    /**
     * Returns a memory from an in-world position. This will look for an override, so looking up an ender chest in-world
     * will return the ender chest's results.
     *
     * @param cbs Client block source describing the position in-world.
     * @return A memory at the given position, considering overrides, or an empty optional otherwise.
     */
    Optional<Memory> getMemory(ClientBlockSource cbs);

    /**
     * Returns a memory from an in-world position. This will look for an override, so looking up an ender chest in-world
     * will return the ender chest's results.
     *
     * @param level Level to look for.
     * @param pos   Position in the level to look at.
     * @return A memory at the given position, considering overrides, or an empty optional otherwise.
     */
    default Optional<Memory> getMemory(Level level, BlockPos pos) {
        return this.getMemory(new CachedClientBlockSource(level, pos));
    }

    /**
     * <p>Helper method for getting a memory from a key and position.</p>
     *
     * <p>This method does not look for an override, so looking up e.g. an ender chest in-world will return nothing. To
     * look for overrides, see {@link #getMemory(ClientBlockSource)}.</p>
     *
     * @param keyId    Memory key to get the memory from.
     * @param position Position to lookup within the memory key.
     * @return A memory at the given key and position, or an empty optional if not present.
     */
    default Optional<Memory> getMemory(ResourceLocation keyId, BlockPos position) {
        return getKey(keyId).flatMap(key -> key.get(position));
    }

    /**
     * <p>Helper method for getting a memory from a key and position.</p>
     *
     * <p>This method does not look for an override, so looking up e.g. an ender chest in-world will return nothing. To
     * look for overrides, see {@link #getMemory(ClientBlockSource)}.</p>
     *
     * @param keyId    Memory key to get the memory from.
     * @param position Position to lookup within the memory key.
     * @return A memory at the given key and position, or an empty optional if not present.
     * @see ProviderUtils#getPlayersCurrentKey()
     */
    default Optional<Memory> getMemory(Optional<ResourceLocation> keyId, BlockPos position) {
        return getKey(keyId).flatMap(key -> key.get(position));
    }

    /**
     * Return the memory key associated with the given memory key, or an empty optional otherwise.
     *
     * @param keyId Memory key ID to lookup.
     * @return An optional possibly containing a matching memory key.
     */
    Optional<MemoryKey> getKey(ResourceLocation keyId);

    /**
     * Convenience overload for {@link #getKey(ResourceLocation)} for faster use with {@link ProviderUtils#getPlayersCurrentKey()}.
     *
     * @param keyId Optional possibly containing a memory key to lookup.
     * @return An optional possibly containing a matching memory key.
     */
    default Optional<MemoryKey> getKey(Optional<ResourceLocation> keyId) {
        return keyId.flatMap(this::getKey);
    }

    /**
     * Helper method for getting a count of items in a given memory key matching a given predicate.
     *
     * @param keyId          Memory key to look in; if non-existent an empty list will be returned.
     * @param predicate      Predicate to filter each memory against.
     * @param stackMergeMode How to merge stacks in the returned list - for more details, see {@link StackMergeMode}
     * @return A list of stacks from all memories in the given key matching the predicate, merged according to <code>stackMergeMode</code>.
     */
    default List<ItemStack> getCounts(ResourceLocation keyId, CountingPredicate predicate, StackMergeMode stackMergeMode) {
        Optional<MemoryKey> key = this.getKey(keyId);
        if (key.isEmpty()) return Collections.emptyList();
        return key.get().getCounts(predicate, stackMergeMode);
    }

    /**
     * Adds or update a memory in this memory bank.
     *
     * @param keyId    Memory key ID that this memory should be added to.
     * @param location Location of the memory in the given key to save at.
     * @param memory   Memory to save.
     * @implNote If the given memory is empty then the memory will be removed instead. This may also delete the whole
     * memory key if empty.
     */
    void addMemory(ResourceLocation keyId, BlockPos location, Memory memory);

    /**
     * Removes a memory from this memory bank. Does nothing in the key or memory does not exist.
     *
     * @param keyId    Memory key ID that this memory should be removed from.
     * @param location Location in the memory key to remove.
     */
    void removeMemory(ResourceLocation keyId, BlockPos location);
}
