package red.jackf.chesttracker.api.provider.memory;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public record MemoryEntry(ResourceLocation key, BlockPos position, MemoryBuilder memory) {
}
