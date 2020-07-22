package red.jackf.chesttracker.gui;

import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ButtonDisplayType;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.mixins.ChestTrackerAccessorHandledScreen;
import red.jackf.chesttracker.tracker.Tracker;

import static red.jackf.chesttracker.ChestTracker.id;

public class ManagerButton extends TexturedButtonWidget {
    private static final Identifier TEXTURE = id("gui_button_small.png");

    public static final int smallWidth = 9;
    public static final int smallHeight = 9;

    public ManagerButton() {
        super(0, 0, smallWidth, smallHeight, 0, 0,0, TEXTURE, 9, 9, (buttonWidget) -> {
            if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen)
                Tracker.getInstance().handleScreen((HandledScreen<?>) MinecraftClient.getInstance().currentScreen);
            MinecraftClient.getInstance().openScreen(new ItemManagerScreen());
        });
    }

    public static void setup() {
        ClothClientHooks.SCREEN_INIT_POST.register((client, screen, screenHooks) -> {
            if (screen instanceof HandledScreen) {
                screenHooks.cloth$addButtonWidget(
                    new ManagerButton()
                );
            }
        });
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        HandledScreen<?> screen = (HandledScreen<?>) MinecraftClient.getInstance().currentScreen;
        if (screen != null)
            this.setPos(getX(screen), getY(screen));

        super.renderButton(matrices, mouseX, mouseY, delta);
    }

    public static int getX(HandledScreen<?> screen) {
        ChestTrackerAccessorHandledScreen accessedScreen = (ChestTrackerAccessorHandledScreen) screen;
        int x = ((MinecraftClient.getInstance().getWindow().getScaledWidth() + accessedScreen.getBackgroundWidth()) / 2) - smallWidth - 5;
        if (adjust())
            x += 77;
        return x;
    }

    public static int getY(HandledScreen<?> screen) {
        ChestTrackerAccessorHandledScreen accessedScreen = (ChestTrackerAccessorHandledScreen) screen;
        return ((MinecraftClient.getInstance().getWindow().getScaledHeight() - accessedScreen.getBackgroundHeight()) / 2) + 5;
    }

    private static boolean adjust() {
        if (MinecraftClient.getInstance().currentScreen instanceof RecipeBookProvider) {
            RecipeBookWidget widget = ((RecipeBookProvider) MinecraftClient.getInstance().currentScreen).getRecipeBookWidget();
            return widget.isOpen();
        }
        return false;
    }
}
