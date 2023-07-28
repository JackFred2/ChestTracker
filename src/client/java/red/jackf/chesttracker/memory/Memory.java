package red.jackf.chesttracker.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.util.ModCodecs;

import java.util.List;

/**
 * List of items. In the future, this may contain a name or other info as well.
 */
public record Memory(List<ItemStack> items) {
    public static final Codec<Memory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(ModCodecs.makeMutableList(ItemStack.CODEC.listOf())
                    .fieldOf("items").forGetter(Memory::items))
                    .apply(instance, Memory::new));

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
