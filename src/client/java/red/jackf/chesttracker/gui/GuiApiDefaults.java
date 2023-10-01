package red.jackf.chesttracker.gui;

import net.minecraft.network.chat.contents.LiteralContents;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.gui.GetCustomName;
import red.jackf.jackfredlib.api.base.ResultHolder;

/**
 * Default handlers for the Gui API events
 */
public class GuiApiDefaults {
    private GuiApiDefaults() {
    }

    public static void setup() {
        GetCustomName.EVENT.register(EventPhases.FALLBACK_PHASE, ((source, screen) -> {
            // if it's not translatable, it's very likely a custom name
            if (screen.getTitle().getContents() instanceof LiteralContents) {
                return ResultHolder.value(screen.getTitle());
            } else {
                return ResultHolder.pass();
            }
        }));
    }
}
