package red.jackf.chesttracker.world;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hands information about a location in a world; used for keeping track of the last interacted block
 */
public record Location(ResourceKey<Level> level, BlockPos pos, BlockState state) {
}
