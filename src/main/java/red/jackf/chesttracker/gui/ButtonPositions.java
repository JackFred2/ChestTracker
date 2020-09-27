package red.jackf.chesttracker.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import red.jackf.chesttracker.mixins.AccessorHandledScreen;
import red.jackf.chesttracker.resource.ButtonPositionManager;

import java.util.Map;

@Environment(EnvType.CLIENT)
public abstract class ButtonPositions {
    public static int getX(HandledScreen<?> screen, int buttonIndex) {
        Map<String, ButtonPositionManager.ButtonPosition> overrides = ButtonPositionManager.getOverrides();
        String className = screen.getClass().getSimpleName();
        if (overrides.containsKey(className)) {
            ButtonPositionManager.ButtonPosition position = overrides.get(className);
            int x = MinecraftClient.getInstance().getWindow().getScaledWidth();
            if (position.horizontalAlignment == ButtonPositionManager.HorizontalAlignment.LEFT) {
                x -= ((AccessorHandledScreen) screen).getBackgroundWidth();
            } else {
                x += ((AccessorHandledScreen) screen).getBackgroundWidth();
            }
            if (doRecipeAdjust(screen)) {
                x += 77 * 2;
            }
            return (x/2) + position.horizontalOffset;
        }
        int x = (MinecraftClient.getInstance().getWindow().getScaledWidth() + ((AccessorHandledScreen) screen).getBackgroundWidth()) / 2;
        if (doRecipeAdjust(screen))
            x += 77;
        return x - 14 - (11 * buttonIndex);
    }

    public static int getY(HandledScreen<?> screen, int buttonIndex) {
        Map<String, ButtonPositionManager.ButtonPosition> overrides = ButtonPositionManager.getOverrides();
        String className = screen.getClass().getSimpleName();
        if (overrides.containsKey(className)) {
            ButtonPositionManager.ButtonPosition position = overrides.get(className);
            int y = MinecraftClient.getInstance().getWindow().getScaledHeight();
            if (position.verticalAlignment == ButtonPositionManager.VerticalAlignment.TOP) {
                y -= ((AccessorHandledScreen) screen).getBackgroundHeight();
            } else {
                y += ((AccessorHandledScreen) screen).getBackgroundHeight();
            }
            return (y/2) + position.verticalOffset;
        }
        int y = (MinecraftClient.getInstance().getWindow().getScaledHeight() - ((AccessorHandledScreen) screen).getBackgroundHeight()) / 2;
        if (!(screen instanceof CreativeInventoryScreen)) {
            y -= 15;
        }
        return y + 5;
    }

    protected static boolean doRecipeAdjust(Screen screen) {
        if (screen instanceof RecipeBookProvider) {
            RecipeBookWidget widget = ((RecipeBookProvider) screen).getRecipeBookWidget();
            return widget.isOpen();
        }
        return false;
    }
}
