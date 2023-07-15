package red.jackf.chesttracker.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.memory.LightweightStack;

public record MemoryIcon(ResourceLocation id, LightweightStack icon) {
    public static final Codec<MemoryIcon> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(ResourceLocation.CODEC.fieldOf("id").forGetter(MemoryIcon::id),
                    LightweightStack.CODEC.fieldOf("icon").forGetter(MemoryIcon::icon)
            ).apply(instance, MemoryIcon::new));
}
