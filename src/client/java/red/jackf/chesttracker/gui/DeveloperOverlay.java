package red.jackf.chesttracker.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.world.LocationTracking;

public class DeveloperOverlay {
    public static void setup() {
        HudRenderCallback.EVENT.register((graphics, delta) -> {
            if (!ChestTrackerConfig.INSTANCE.getConfig().gui.showDevHud) return;
            graphics.drawString(Minecraft.getInstance().font, "Location: " + LocationTracking.peekLocation(), 10, 10, 0xFF_FFFFFF);
        });
    }
}
