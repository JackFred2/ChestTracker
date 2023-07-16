package red.jackf.chesttracker.memory;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.world.Location;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ScreenHandler {
    public static void handle(Location loc, AbstractContainerScreen<?> screen) {
        if (!isValidScreen(screen)) return;
        if (ItemMemory.INSTANCE == null) return;
        ItemMemory.INSTANCE.addMemory(loc.level().location(), loc.pos(), new LocationData(getItems(screen)));
    }

    private static List<ItemStack> getItems(AbstractContainerScreen<?> screen) {
        return screen.getMenu().slots.stream()
                .filter(ScreenHandler::isValidSlot)
                .map(Slot::getItem)
                .filter(Predicate.not(ItemStack::isEmpty))
                .collect(Collectors.toList());
    }

    private static boolean isValidSlot(Slot slot) {
        return !(slot.container instanceof Inventory);
    }

    private static boolean isValidScreen(AbstractContainerScreen<?> screen) {
        return !(screen instanceof EffectRenderingInventoryScreen<?>);
    }
}
