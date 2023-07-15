package red.jackf.chesttracker.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Fake itemstack that has no count and easier equality check
 */
public record LightweightStack(Item item, @Nullable CompoundTag tag) {
    public static final Codec<LightweightStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(LightweightStack::item),
            CompoundTag.CODEC.optionalFieldOf("tag").forGetter(stack -> Optional.ofNullable(stack.tag()))
        ).apply(instance, LightweightStack::new));

    public LightweightStack(Item item) {
        this(item, (CompoundTag) null);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private LightweightStack(Item item, Optional<CompoundTag> tag) {
        this(item, tag.orElse(null));
    }

    public ItemStack toStack() {
        var stack = new ItemStack(item);
        stack.setTag(tag);
        return stack;
    }
}
