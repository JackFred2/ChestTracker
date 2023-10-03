package red.jackf.chesttracker.api.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.util.ModCodecs;

/**
 * Represents a memory key ID - ItemStack pair, used for icons in the GUI.
 *
 * @param id Memory Key ID this icon is for.
 * @param icon ItemStack to be displayed as the icon for this key.
 */
public record MemoryKeyIcon(ResourceLocation id, ItemStack icon) {
    public static final Codec<MemoryKeyIcon> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(ResourceLocation.CODEC.fieldOf("id").forGetter(MemoryKeyIcon::id),
                   ModCodecs.ITEM_STACK_IGNORE_COUNT.fieldOf("icon").forGetter(MemoryKeyIcon::icon)
            ).apply(instance, MemoryKeyIcon::new));
}
