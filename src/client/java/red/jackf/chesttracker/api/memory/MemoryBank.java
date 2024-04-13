package red.jackf.chesttracker.api.memory;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.api.memory.counting.CountingPredicate;
import red.jackf.chesttracker.api.memory.counting.StackMergeMode;
import red.jackf.chesttracker.api.providers.ProviderUtils;

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
     * Helper method for getting a memory from a key and position.
     *
     * @param keyId Memory key to get the memory from.
     * @param position Position to lookup within the memory key.
     * @return A memory at the given key and position, or an empty optional if not present.
     */
    default Optional<Memory> getMemory(ResourceLocation keyId, BlockPos position) {
        return getKey(keyId).flatMap(key -> key.get(position));
    }

    /**
     * Helper method for getting a memory from a key and position.
     *
     * @see ProviderUtils#getPlayersCurrentKey()
     *
     * @param keyId Memory key to get the memory from.
     * @param position Position to lookup within the memory key.
     * @return A memory at the given key and position, or an empty optional if not present.
     */
    default Optional<Memory> getMemory(Optional<ResourceLocation> keyId, BlockPos position) {
        return getKey(keyId).flatMap(key -> key.get(position));
    }

    default List<ItemStack> getCounts(ResourceLocation keyId, CountingPredicate predicate, StackMergeMode stackMergeMode) {
        Optional<MemoryKey> key = this.getKey(keyId);
        if (key.isEmpty()) return Collections.emptyList();
        return key.get().getCounts(predicate, stackMergeMode);
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
     * Adds or update a memory in this memory bank.
     *
     * @implNote If the given memory is empty then the memory will be removed instead. This may also delete the whole
     * memory key if empty.
     *
     * @param keyId Memory key ID that this memory should be added to.
     * @param location Location of the memory in the given key to save at.
     * @param memory Memory to save.
     */
    void addMemory(ResourceLocation keyId, BlockPos location, Memory memory);

    /**
     * Removes a memory from this memory bank. Does nothing in the key or memory does not exist.
     *
     * @param keyId Memory key ID that this memory should be removed from.
     * @param location Location in the memory key to remove.
     */
    void removeMemory(ResourceLocation keyId, BlockPos location);
}
