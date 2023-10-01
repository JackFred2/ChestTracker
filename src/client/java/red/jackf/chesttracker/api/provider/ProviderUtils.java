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

/**
 * Utilities for creating a custom {@link Provider} instance.
 *
 * @see InteractionTracker#INSTANCE
 * @see red.jackf.whereisit.api.search.ConnectedBlocksGrabber
 */
public class ProviderUtils {
    private ProviderUtils() {}

    /**
     * Pulls a list of all item stacks from a container screen, and filters out any from the player's inventory.
     *
     * @param screen Screen to pull items from.
     * @return A list of all item stacks not in a player's inventory.
     */
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

    /**
     * Checks whether a given ClientBlockSource should be remembered, based on the current memory bank's filtering
     * rules.
     *
     * @param source Source to test against.
     * @return Whether, according to default provider rules, this block source should be remembered.
     */
    public static boolean defaultShouldRemember(ClientBlockSource source) {
        FilteringSettings settings = Optional.ofNullable(MemoryBank.INSTANCE).map(bank -> bank.getMetadata().getFilteringSettings()).orElse(null);
        if (settings == null) return false;
        if (!settings.rememberedContainers.predicate.test(source.blockState())) return false;
        return source.blockEntity() instanceof MenuProvider;
    }

    /**
     * Tests whether a given Slot from a container screen is part of a player's inventory.
     *
     * @param slot Slot to test.
     * @return Whether the given slot is part of a player's inventory.
     */
    public static boolean isSlotPlayerInventory(Slot slot) {
        return slot.container instanceof Inventory;
    }
}
