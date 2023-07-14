package red.jackf.chesttracker.memory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Fake itemstack that has no count and easier equality check
 */
public record LightweightStack(Item item, @Nullable CompoundTag tag) {
    public LightweightStack(Item item) {
        this(item, null);
    }

    public ItemStack toStack() {
        var stack = new ItemStack(item);
        stack.setTag(tag);
        return stack;
    }
}
