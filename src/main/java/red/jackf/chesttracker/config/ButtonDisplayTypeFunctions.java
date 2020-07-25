package red.jackf.chesttracker.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import red.jackf.chesttracker.mixins.ChestTrackerAccessorHandledScreen;

import java.util.function.Function;

import static red.jackf.chesttracker.gui.ManagerButton.smallWidth;

public abstract class ButtonDisplayTypeFunctions {
    // ABOVE_RIGHT, TOP_RIGHT, BOTTOM_RIGHT
    protected static final Function<HandledScreen<?>, Integer> rightX = screen -> {
        ChestTrackerAccessorHandledScreen accessedScreen = (ChestTrackerAccessorHandledScreen) screen;
        int x = ((MinecraftClient.getInstance().getWindow().getScaledWidth() + accessedScreen.getBackgroundWidth()) / 2) - smallWidth - 7;
        if (adjust())
            x += 77;
        return x;
    };

    // MIDDLE_RIGHT
    protected static final Function<HandledScreen<?>, Integer> rightXInvProfShift = screen -> rightX.apply(screen) - 12;

    // ABOVE_LEFT, BOTTOM_LEFT
    protected static final Function<HandledScreen<?>, Integer> leftX = screen -> {
        ChestTrackerAccessorHandledScreen accessedScreen = (ChestTrackerAccessorHandledScreen) screen;
        int x = ((MinecraftClient.getInstance().getWindow().getScaledWidth() - accessedScreen.getBackgroundWidth()) / 2) + 18;
        if (adjust())
            x += 77;
        if (screen instanceof CreativeInventoryScreen)
            x -= 29;
        return x;
    };

    // LEFT_VERTICAL
    protected static final Function<HandledScreen<?>, Integer> leftVertX = screen -> {
        ChestTrackerAccessorHandledScreen accessedScreen = (ChestTrackerAccessorHandledScreen) screen;
        int x = ((MinecraftClient.getInstance().getWindow().getScaledWidth() - accessedScreen.getBackgroundWidth()) / 2) - 11;
        return x;
    };

    protected static final Function<HandledScreen<?>, Integer> topLeftVertY = screen -> {
        ChestTrackerAccessorHandledScreen accessedScreen = (ChestTrackerAccessorHandledScreen) screen;
        return ((MinecraftClient.getInstance().getWindow().getScaledHeight() - accessedScreen.getBackgroundHeight()) / 2) + 5;
    };

    protected static final Function<HandledScreen<?>, Integer> bottomLeftVertY = screen -> {
        ChestTrackerAccessorHandledScreen accessedScreen = (ChestTrackerAccessorHandledScreen) screen;
        return ((MinecraftClient.getInstance().getWindow().getScaledHeight() + accessedScreen.getBackgroundHeight()) / 2) - 24;
    };

    protected static final Function<HandledScreen<?>, Integer> topY = screen -> {
        ChestTrackerAccessorHandledScreen accessedScreen = (ChestTrackerAccessorHandledScreen) screen;
        return ((MinecraftClient.getInstance().getWindow().getScaledHeight() - accessedScreen.getBackgroundHeight()) / 2) + 5;
    };

    protected static final Function<HandledScreen<?>, Integer> bottomRightY = screen -> {
        if (screen instanceof CreativeInventoryScreen) return topY.apply(screen);
        ChestTrackerAccessorHandledScreen accessedScreen = (ChestTrackerAccessorHandledScreen) screen;
        return ((MinecraftClient.getInstance().getWindow().getScaledHeight() + accessedScreen.getBackgroundHeight()) / 2) + 1;
    };

    protected static final Function<HandledScreen<?>, Integer> bottomLeftY = screen -> {
        ChestTrackerAccessorHandledScreen accessedScreen = (ChestTrackerAccessorHandledScreen) screen;
        return ((MinecraftClient.getInstance().getWindow().getScaledHeight() + accessedScreen.getBackgroundHeight()) / 2) + 1;
    };

    protected static final Function<HandledScreen<?>, Integer> aboveY = screen -> {
        if (screen instanceof CreativeInventoryScreen) return topY.apply(screen);
        ChestTrackerAccessorHandledScreen accessedScreen = (ChestTrackerAccessorHandledScreen) screen;
        return ((MinecraftClient.getInstance().getWindow().getScaledHeight() - accessedScreen.getBackgroundHeight()) / 2) - 11;
    };

    protected static final Function<HandledScreen<?>, Integer> middleY = screen -> {
        if (screen instanceof CreativeInventoryScreen) return topY.apply(screen);
        ChestTrackerAccessorHandledScreen accessedScreen = (ChestTrackerAccessorHandledScreen) screen;
        return ((MinecraftClient.getInstance().getWindow().getScaledHeight() + accessedScreen.getBackgroundHeight()) / 2) - 95;
    };

    protected static boolean adjust() {
        if (MinecraftClient.getInstance().currentScreen instanceof RecipeBookProvider) {
            RecipeBookWidget widget = ((RecipeBookProvider) MinecraftClient.getInstance().currentScreen).getRecipeBookWidget();
            return widget.isOpen();
        }
        return false;
    }
}
