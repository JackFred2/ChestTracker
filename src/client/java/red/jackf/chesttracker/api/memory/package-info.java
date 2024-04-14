/**
 * <p>Contains classes for working with Chest Tracker's Memory Banks. Instances of the current memory bank can be obtained
 * from {@link red.jackf.chesttracker.api.memory.MemoryBankAccess#getLoaded()}.</p>
 *
 * <p>The structure of a Memory Bank has 3 tiers:
 * <ol>
 *     <li>The top level <b>Memory Bank</b> object, containing:</li>
 *     <li>A map of Memory Key IDs (such as minecraft:overworld) to <b>Memory Key</b>s, each of which:</li>
 *     <li>A map of Block Positions to <b>Memory</b> objects</li>
 * </ol>
 * Each Memory contains details such as a list of item stacks, the name (user-defined and ripped), connected positions and
 * more.</p>
 *
 * <h1>TL;DR</h1>
 *
 * <p>If you just want to get a memory from the world and a position, use
 * {@link red.jackf.chesttracker.api.memory.MemoryBank#getMemory(net.minecraft.world.level.Level, net.minecraft.core.BlockPos)}</p>
 *
 * <p>If you want to get a list of all items in the current world, possibly within a range, use
 * {@link red.jackf.chesttracker.api.memory.MemoryBank#getCounts(
 *            net.minecraft.resources.ResourceLocation,
 *            red.jackf.chesttracker.api.memory.counting.CountingPredicate,
 *            red.jackf.chesttracker.api.memory.counting.StackMergeMode
 * )} along with a desired predicate.</p>
 *
 * @see red.jackf.chesttracker.api.memory.MemoryBank
 * @see red.jackf.chesttracker.api.memory.MemoryKey
 * @see red.jackf.chesttracker.api.memory.Memory
 */
package red.jackf.chesttracker.api.memory;