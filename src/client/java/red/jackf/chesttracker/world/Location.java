package red.jackf.chesttracker.world;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record Location(ResourceKey<Level> level, BlockPos pos) {
}
