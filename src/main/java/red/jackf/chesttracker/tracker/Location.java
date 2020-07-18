package red.jackf.chesttracker.tracker;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class Location {
    private final BlockPos position;
    @Nullable
    private Text name;
    private List<ItemStack> items;

    public Location(BlockPos position, @Nullable Text name, List<ItemStack> items) {
        this.position = position;
        this.name = name;
        this.items = items;
    }

    public BlockPos getPosition() {
        return position;
    }

    @Nullable
    public Text getName() {
        return name;
    }

    public void setName(@Nullable Text name) {
        this.name = name;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return position.equals(location.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position);
    }

    @Override
    public String toString() {
        return "Location{" +
                "position=" + position +
                ", name=" + name +
                ", items=" + items +
                '}';
    }
}
