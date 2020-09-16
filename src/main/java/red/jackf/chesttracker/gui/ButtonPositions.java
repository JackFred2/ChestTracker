package red.jackf.chesttracker.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import red.jackf.chesttracker.mixins.AccessorHandledScreen;

@Environment(EnvType.CLIENT)
public abstract class ButtonPositions {
    public static int getX(HandledScreen<?> screen, int buttonIndex) {
        int x = (MinecraftClient.getInstance().getWindow().getScaledWidth() + ((AccessorHandledScreen) screen).getBackgroundWidth()) / 2;
        if (doRecipeAdjust(screen))
            x += 77;
        return x - 14 - (11 * buttonIndex);
    }

    public static int getY(HandledScreen<?> screen, int buttonIndex) {
        int y = (MinecraftClient.getInstance().getWindow().getScaledHeight() - ((AccessorHandledScreen) screen).getBackgroundHeight()) / 2;
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
