package red.jackf.chesttracker.config;

import net.minecraft.client.gui.screen.ingame.HandledScreen;

public class LibGuiHandler {
    public static boolean cancel(HandledScreen<?> screen) {
        if (COTTON_INVENTORY_SCREEN == null) return false;
        return COTTON_INVENTORY_SCREEN.isAssignableFrom(screen.getClass());
    }

    private static final Class<?> COTTON_INVENTORY_SCREEN;

    static {
        Class<?> COTTON_INVENTORY_SCREEN1;
        try {
            COTTON_INVENTORY_SCREEN1 = Class.forName("io.github.cottonmc.gui.client.CottonInventoryScreen");
        } catch (Throwable e) {
            COTTON_INVENTORY_SCREEN1 = null;
        }
        COTTON_INVENTORY_SCREEN = COTTON_INVENTORY_SCREEN1;
    }
}
