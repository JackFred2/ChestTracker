package red.jackf.chesttracker.gui;

import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.gui.GetCustomName;
import red.jackf.chesttracker.api.gui.ScreenBlacklist;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.jackfredlib.api.base.ResultHolder;

/**
 * Default handlers for the Gui API events
 */
public class GuiApiDefaults {
    private GuiApiDefaults() {
    }

    public static void setup() {
        GetCustomName.EVENT.register(EventPhases.FALLBACK_PHASE, ((source, screen) -> {
            if (MemoryBank.INSTANCE == null) return ResultHolder.pass();
            var title = screen.getTitle();

            if (containsTranslatable(title)) return ResultHolder.pass();

            // if it's not translatable, it's very likely a custom name
            return ResultHolder.value(MemoryBank.INSTANCE.getMetadata().getCompatibilitySettings().nameFilterMode.filter.apply(title));
        }));

        ScreenBlacklist.add(
            EffectRenderingInventoryScreen.class,
            BeaconScreen.class
        );
    }

    // Visits without decomposing
    private static boolean containsTranslatable(Component component) {
        if (component.getContents() instanceof TranslatableContents) return true;
        for (Component sibling : component.getSiblings()) {
            if (containsTranslatable(sibling)) return true;
        }
        return false;
    }
}
