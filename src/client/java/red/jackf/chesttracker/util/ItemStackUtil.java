package red.jackf.chesttracker.util;

import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.memory.LightweightStack;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ItemStackUtil {
    /**
     * Combine and sort a list of ItemStacks in descending order of count
     */
    public static List<ItemStack> flattenStacks(List<ItemStack> in) {
        var counts = new HashMap<LightweightStack, Integer>();
        for (ItemStack itemStack : in) {
            counts.merge(new LightweightStack(itemStack), itemStack.getCount(), Integer::sum);
        }
        return counts.entrySet().stream()
                .map(entry -> {
                    var stack = entry.getKey().toStack();
                    stack.setCount(entry.getValue());
                    return stack;
                }).sorted(Comparator.comparingInt(ItemStack::getCount).reversed())
                .toList();
    }
}
