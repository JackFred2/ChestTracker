package red.jackf.chesttracker.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record MemoryKeyIcon(ResourceLocation id, ItemStack icon) {
    public static final Codec<MemoryKeyIcon> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(ResourceLocation.CODEC.fieldOf("id").forGetter(MemoryKeyIcon::id),
                    ItemStack.CODEC.fieldOf("icon").forGetter(MemoryKeyIcon::icon)
            ).apply(instance, MemoryKeyIcon::new));
}
