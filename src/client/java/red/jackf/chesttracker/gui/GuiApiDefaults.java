package red.jackf.chesttracker.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.ResultHolder;
import red.jackf.chesttracker.api.gui.GetCustomName;
import red.jackf.chesttracker.api.gui.GetMemory;
import red.jackf.chesttracker.memory.Memory;

import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Default handlers for the Gui API events
 */
public class GuiApiDefaults {
    private GuiApiDefaults() {}

    public static void setup() {
        // default, "grab everything from the slots that's not from the player's inventory"
        GetMemory.EVENT.register(EventPhases.FALLBACK_PHASE, (location, screen, level) -> {
            Component name = GetCustomName.EVENT.invoker().getName(location, screen).getNullable();
            return ResultHolder.value(new Memory(screen.getMenu().slots.stream()
                    .filter(GuiApiDefaults::isValidSlot)
                    .map(Slot::getItem)
                    .filter(Predicate.not(ItemStack::isEmpty))
                    .collect(Collectors.toList()), name));
        });

        GetCustomName.EVENT.register(EventPhases.FALLBACK_PHASE, ((location, screen) -> {
            // if it's not translatable, it's very likely a custom name
            if (screen.getTitle().getContents() instanceof LiteralContents) {
                return ResultHolder.value(screen.getTitle());
            } else {
                return ResultHolder.pass();
            }
        }));
    }

    private static boolean isValidSlot(Slot slot) {
        return !(slot.container instanceof Inventory);
    }
}
