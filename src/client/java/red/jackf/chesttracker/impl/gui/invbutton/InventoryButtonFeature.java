package red.jackf.chesttracker.impl.gui.invbutton;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.server.packs.PackType;
import red.jackf.chesttracker.api.providers.ProviderUtils;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.chesttracker.impl.gui.invbutton.data.InventoryButtonPositionLoader;
import red.jackf.chesttracker.impl.gui.invbutton.ui.InventoryButton;
import red.jackf.chesttracker.impl.providers.ScreenOpenContextImpl;

import java.util.Optional;

/**
 * Handles data loading and screen events for the button.
 */
public class InventoryButtonFeature {
    public static void setup() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new InventoryButtonPositionLoader());
    }

    public static void onScreenOpen(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        if (!ChestTrackerConfig.INSTANCE.instance().gui.inventoryButton.enabled) return;
        if (screen instanceof AbstractContainerScreen<?> menuScreen) {
            var position = ButtonPositionMap.getPositionFor(menuScreen);

            var target = ProviderUtils.getCurrentProvider().flatMap(provider -> {
                ScreenOpenContextImpl openContext = ScreenOpenContextImpl.createFor(menuScreen);

                provider.onScreenOpen(openContext);

                return Optional.ofNullable(openContext.getTarget());
            });

            InventoryButton button = new InventoryButton(menuScreen, position, target);

            ((CTButtonScreenDuck) menuScreen).chesttracker$setButton(button);

            // add to start so interactions happen first
            Screens.getButtons(menuScreen).add(0, button);
        }
    }
}
