package red.jackf.chesttracker.api.location;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/**
 * Hands information about a location in a world; used for keeping track of the last interacted block
 */
public record Location(ResourceLocation key, BlockPos pos) {}
