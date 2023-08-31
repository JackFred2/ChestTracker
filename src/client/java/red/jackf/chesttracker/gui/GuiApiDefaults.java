package red.jackf.chesttracker.gui;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.gui.GetCustomName;
import red.jackf.chesttracker.api.gui.GetMemory;
import red.jackf.chesttracker.memory.Memory;
import red.jackf.jackfredlib.api.ResultHolder;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default handlers for the Gui API events
 */
public class GuiApiDefaults {
    private GuiApiDefaults() {}

    public static void setup() {
        // default, "grab everything from the slots that's not from the player's inventory"
        GetMemory.EVENT.register(EventPhases.FALLBACK_PHASE, (location, screen, level) -> {
            List<ItemStack> items = new ArrayList<>();

            for (Slot slot : screen.getMenu().slots) {
                if (isValidSlot(slot)) {
                    ItemStack item = slot.getItem();
                    if (!item.isEmpty()) {
                        items.add(item);
                    }
                }
            }

            // get connected, minus the original pos
            List<BlockPos> connected = Collections.emptyList();
            if (level.dimension().location().equals(location.key()))
                connected = ConnectedBlocksGrabber.getConnected(level, level.getBlockState(location.pos()), location.pos())
                        .stream()
                        .filter(pos -> !pos.equals(location.pos()))
                        .toList();

            return ResultHolder.value(Memory.builder(items)
                    .name(GetCustomName.EVENT.invoker().getName(location, screen).getNullable())
                    .otherPositions(connected)
                    .build());
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
