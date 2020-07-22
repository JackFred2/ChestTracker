package red.jackf.chesttracker.tracker;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class Location {
    private final BlockPos position;
    @Nullable
    private final Text name;
    @Nullable
    private final Vec3d nameOffset;
    private final List<ItemStack> items;

    public Location(BlockPos position, @Nullable Text name, @Nullable Vec3d nameOffset, List<ItemStack> items) {
        this.position = position;
        this.name = name;
        this.nameOffset = nameOffset;
        this.items = items;
    }

    public BlockPos getPosition() {
        return position;
    }

    @Nullable
    public Text getName() {
        return name;
    }

    public List<ItemStack> getItems() {
        return items;
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
            ", nameOffset=" + nameOffset +
            ", items=" + items +
            '}';
    }

    @Nullable
    public Vec3d getNameOffset() {
        return nameOffset;
    }

    public boolean hasNameOffset() {
        return nameOffset != null;
    }
}
