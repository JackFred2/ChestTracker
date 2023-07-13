package red.jackf.chesttracker.config;

import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.memory.LightweightStack;

public record LevelIcon(ResourceLocation id, LightweightStack icon) {
}
