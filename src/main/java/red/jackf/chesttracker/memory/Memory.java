package red.jackf.chesttracker.memory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;
/*
    A representation of a saved location, along with it's items.
 */
@Environment(EnvType.CLIENT)
public class Memory {
    @Nullable
    private final BlockPos position;
    private final List<ItemStack> items;
    @Nullable
    private final Text title;

    private Memory(@Nullable BlockPos position, List<ItemStack> items, @Nullable Text title) {
        this.position = position;
        this.items = items;
        this.title = title;
    }

    public static Memory of(@Nullable BlockPos pos, List<ItemStack> items, @Nullable Text title) {
        return new Memory(pos, items, title);
    }

    @Nullable
    public BlockPos getPosition() {
        return position;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    @Nullable
    public Text getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Memory{" +
            "position=" + position +
            ", items=" + items +
            ", title=" + title +
            '}';
    }
}
