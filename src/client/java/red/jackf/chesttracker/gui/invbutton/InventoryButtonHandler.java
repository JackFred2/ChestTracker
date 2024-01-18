package red.jackf.chesttracker.gui.invbutton;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import red.jackf.chesttracker.mixins.AbstractContainerScreenAccessor;

import java.util.Set;

public class InventoryButtonHandler {
    public static void setup() {
        Set<Class<? extends AbstractContainerScreen<?>>> validClasses = Set.of(InventoryScreen.class);

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?> menuScreen && validClasses.contains(screen.getClass())) {
                int left = ((AbstractContainerScreenAccessor) menuScreen).chesttracker$getLeft();
                int top = ((AbstractContainerScreenAccessor) menuScreen).chesttracker$getTop();
                int x = left - 11;
                int y = top - 11;

                Screens.getButtons(menuScreen).add(new InventoryButton(menuScreen, x, y));
            }
        });


    }
}
