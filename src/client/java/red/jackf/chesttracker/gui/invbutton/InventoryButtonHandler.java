package red.jackf.chesttracker.gui.invbutton;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.server.packs.PackType;
import red.jackf.chesttracker.gui.invbutton.data.InventoryButtonPositionLoader;

public class InventoryButtonHandler {
    public static void setup() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?> menuScreen) {
                var position = ButtonPositionTracker.INSTANCE.getFor(menuScreen);

                Screens.getButtons(menuScreen).add(
                        new InventoryButton(menuScreen, () -> position.getX(menuScreen), position.getY(menuScreen))
                );
            }
        });

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new InventoryButtonPositionLoader());
    }
}
