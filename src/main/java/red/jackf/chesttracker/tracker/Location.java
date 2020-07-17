package red.jackf.chesttracker.tracker;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class Location {
    private final BlockPos position;
    private Text name;
    private List<ItemStack> items;

    public Location(BlockPos position, Text name, List<ItemStack> items) {
        this.position = position;
        this.name = name;
        this.items = items;
    }

    public BlockPos getPosition() {
        return position;
    }

    public Text getName() {
        return name;
    }

    public void setName(Text name) {
        this.name = name;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
    }
}
