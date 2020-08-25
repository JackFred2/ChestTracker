package red.jackf.chesttracker.memory;

import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LightweightStack {
    private final Item item;
    @Nullable
    private final CompoundTag tag;

    public LightweightStack(Item item, CompoundTag tag) {
        this.item = item;
        this.tag = tag;
    }

    public Item getItem() {
        return item;
    }

    @Nullable
    public CompoundTag getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LightweightStack that = (LightweightStack) o;
        return item.equals(that.item) &&
            Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, tag);
    }

    @Override
    public String toString() {
        return "LightweightStack{" +
            "item=" + item +
            ", tag=" + tag +
            '}';
    }
}
