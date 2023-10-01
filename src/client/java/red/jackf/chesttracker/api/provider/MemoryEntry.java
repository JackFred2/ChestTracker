package red.jackf.chesttracker.api.provider;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.api.gui.MemoryBuilder;

public record MemoryEntry(ResourceLocation key, BlockPos position, MemoryBuilder memory) {
}
