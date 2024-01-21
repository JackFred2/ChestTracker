package red.jackf.chesttracker.gui.invbutton;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.server.packs.PackType;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.invbutton.data.InventoryButtonPositionLoader;
import red.jackf.chesttracker.gui.invbutton.ui.InventoryButton;

/**
 * Handles data loading and screen events for the button.
 */
public class InventoryButtonFeature {
    public static void setup() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!ChestTrackerConfig.INSTANCE.instance().gui.inventoryButton.enabled) return;
            if (screen instanceof AbstractContainerScreen<?> menuScreen) {
                var position = ButtonPositionMap.getPositionFor(menuScreen);

                InventoryButton button = new InventoryButton(menuScreen, position);

                ((CTScreenDuck) menuScreen).chesttracker$setButton(button);

                // add to start so interactions happen first
                Screens.getButtons(menuScreen).add(0, button);
            }
        });

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new InventoryButtonPositionLoader());
    }
}
