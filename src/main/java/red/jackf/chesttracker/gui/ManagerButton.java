package red.jackf.chesttracker.gui;

import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ButtonDisplayType;
import red.jackf.chesttracker.tracker.Tracker;

import static red.jackf.chesttracker.ChestTracker.id;

@Environment(EnvType.CLIENT)
public class ManagerButton extends TexturedButtonWidget {
    private static final Identifier TEXTURE = id("gui_button_small.png");

    public static final int smallWidth = 9;
    public static final int smallHeight = 9;

    public ManagerButton() {
        super(0, 0, smallWidth, smallHeight, 0, 0, 0, TEXTURE, 9, 9, (buttonWidget) -> {
            if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen)
                Tracker.getInstance().handleScreen((HandledScreen<?>) MinecraftClient.getInstance().currentScreen);
            MinecraftClient.getInstance().openScreen(new ItemManagerScreen());
        });
    }

    public static void setup() {
        ClothClientHooks.SCREEN_INIT_POST.register((client, screen, screenHooks) -> {
            if (screen instanceof HandledScreen && !(screen instanceof CreativeInventoryScreen)) {
                screenHooks.cloth$addButtonWidget(
                    new ManagerButton()
                );
            }
        });
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        HandledScreen<?> screen = (HandledScreen<?>) MinecraftClient.getInstance().currentScreen;
        ButtonDisplayType type = ChestTracker.CONFIG.visualOptions.buttonDisplayType;
        if (screen != null)
            this.setPos(type.getX(screen), type.getY(screen));

        super.renderButton(matrices, mouseX, mouseY, delta);
    }
}
