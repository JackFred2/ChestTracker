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
import red.jackf.chesttracker.mixins.ChestTrackerAccessorHandledScreen;
import red.jackf.chesttracker.tracker.Tracker;

import static red.jackf.chesttracker.ChestTracker.id;

public class ManagerButton extends TexturedButtonWidget {
    private static final Identifier TEXTURE = id("gui_button.png");
    private static final Identifier TEXTURE_SMALL = id("gui_button_small.png");

    public static final int bigWidth = 20;
    public static final int bigHeight = 20;

    public static final int smallWidth = 9;
    public static final int smallHeight = 9;

    private boolean big = false;

    public ManagerButton(boolean big) {
        super(0, 0, big ? bigWidth : smallWidth, big ? bigHeight : smallHeight, 0, 0, big ? bigHeight : 0, big ? TEXTURE : TEXTURE_SMALL, 256, 256, (buttonWidget) -> {
            if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen)
                Tracker.getInstance().handleScreen((HandledScreen<?>) MinecraftClient.getInstance().currentScreen);
            MinecraftClient.getInstance().openScreen(new ItemManagerScreen());
        });
        this.big = big;
    }

    public static void setup() {
        ClothClientHooks.SCREEN_INIT_POST.register((client, screen, screenHooks) -> {
            if (screen instanceof HandledScreen) {
                screenHooks.cloth$addButtonWidget(new ManagerButton(MinecraftClient.getInstance().getWindow().getScaledHeight() > bigHeight + getY((HandledScreen<?>) screen, true)));
            }
        });
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        HandledScreen<?> screen = (HandledScreen<?>) MinecraftClient.getInstance().currentScreen;
        if (screen != null)
            this.setPos(getX(screen, big), getY(screen, big));

        super.renderButton(matrices, mouseX, mouseY, delta);
    }

    public static int getX(HandledScreen<?> screen, boolean big) {
        ChestTrackerAccessorHandledScreen accessedScreen = (ChestTrackerAccessorHandledScreen) screen;
        if (big) {
            int x = ((MinecraftClient.getInstance().getWindow().getScaledWidth() + accessedScreen.getBackgroundWidth()) / 2) - bigWidth;
            if (screen instanceof CreativeInventoryScreen) x -= 29;
            if (adjust())
                x += 77;
            return x;
        } else {
            int x = ((MinecraftClient.getInstance().getWindow().getScaledWidth() + accessedScreen.getBackgroundWidth()) / 2) - smallWidth - 5;
            if (adjust())
                x += 77;
            return x;
        }
    }

    public static int getY(HandledScreen<?> screen, boolean big) {
        ChestTrackerAccessorHandledScreen accessedScreen = (ChestTrackerAccessorHandledScreen) screen;
        if (big) {
            return ((MinecraftClient.getInstance().getWindow().getScaledHeight() + accessedScreen.getBackgroundHeight()) / 2) + 1;
        } else {
            return ((MinecraftClient.getInstance().getWindow().getScaledHeight() - accessedScreen.getBackgroundHeight()) / 2) + 5;
        }
    }

    private static boolean adjust() {
        if (MinecraftClient.getInstance().currentScreen instanceof RecipeBookProvider) {
            RecipeBookWidget widget = ((RecipeBookProvider) MinecraftClient.getInstance().currentScreen).getRecipeBookWidget();
            return widget.isOpen();
        }
        return false;
    }
}
