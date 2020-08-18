package red.jackf.chesttracker.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import red.jackf.chesttracker.mixins.AccessorHandledScreen;

public abstract class ButtonPositions {
    public static int getX(HandledScreen<?> screen, int buttonIndex) {
        int left = (((AccessorHandledScreen) screen).getBackgroundWidth() + MinecraftClient.getInstance().getWindow().getScaledWidth())/2;
        return left + (11 * buttonIndex);
    }

    public static int getY(HandledScreen<?> screen, int buttonIndex) {
        int top = (((AccessorHandledScreen) screen).getBackgroundHeight() + MinecraftClient.getInstance().getWindow().getScaledHeight())/2;
        return top;
    }
}
