package red.jackf.chesttracker.api.provider;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.memory.metadata.FilteringSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProviderUtils {
    private ProviderUtils() {}

    public static List<ItemStack> getNonPlayerStacks(AbstractContainerScreen<?> screen) {
        List<ItemStack> items = new ArrayList<>();

        for (Slot slot : screen.getMenu().slots) {
            if (!isSlotPlayerInventory(slot)) {
                ItemStack item = slot.getItem();
                if (!item.isEmpty()) {
                    items.add(item);
                }
            }
        }

        return items;
    }

    public static boolean defaultShouldRemember(ClientBlockSource source) {
        FilteringSettings settings = Optional.ofNullable(MemoryBank.INSTANCE).map(bank -> bank.getMetadata().getFilteringSettings()).orElse(null);
        if (settings == null) return false;
        if (!settings.rememberedContainers.predicate.test(source.blockState())) return false;
        return source.blockEntity() instanceof MenuProvider;
    }

    public static boolean isSlotPlayerInventory(Slot slot) {
        return slot.container instanceof Inventory;
    }
}
