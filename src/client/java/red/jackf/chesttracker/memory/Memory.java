package red.jackf.chesttracker.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.util.ModCodecs;

import java.util.List;
import java.util.Optional;

/**
 * List of information for a location.
 */
public record Memory(List<ItemStack> items, @Nullable Component name) {

    public static final Codec<Memory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ModCodecs.makeMutableList(ItemStack.CODEC.listOf()).fieldOf("items").forGetter(Memory::items),
                    ExtraCodecs.COMPONENT.optionalFieldOf("name").forGetter(m -> Optional.ofNullable(m.name()))
            ).apply(instance, (items, nameOpt) -> new Memory(items, nameOpt.orElse(null))));

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
