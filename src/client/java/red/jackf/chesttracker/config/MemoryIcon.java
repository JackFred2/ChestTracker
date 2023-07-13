package red.jackf.chesttracker.config;

import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.memory.LightweightStack;

public record MemoryIcon(ResourceLocation id, LightweightStack icon) {
}
