package red.jackf.chesttracker.gui;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import static red.jackf.chesttracker.ChestTracker.id;

@Environment(EnvType.CLIENT)
public class ButtonWidgets extends TexturedButtonWidget {
    private static final Identifier TEXTURE = id("textures/gui_button_small.png");
    private static final Identifier BACKGROUND_TEXTURE = id("textures/gui_button_background.png");
    private static final Identifier NAME_EDIT_TEXTURE = id("textures/text_button.png");
    private final HandledScreen<?> screen;
    private boolean wasHovered = false;

    public ButtonWidgets(HandledScreen<?> screen) {
        super(0, 0, 9, 9, 0, 0, 0, TEXTURE, 9, 44, (button) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen != null) client.currentScreen.onClose();
            client.openScreen(new ItemListScreen());
        });
        this.screen = screen;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.reposition();
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

        // render
        if (this.visible) {
            if (this.wasHovered != this.isHovered()) {
                if (this.isHovered()) {
                    if (this.isFocused()) {
                        this.queueNarration(200);
                    } else {
                        this.queueNarration(750);
                    }
                } else {
                    this.nextNarration = Long.MAX_VALUE;
                }
            }

            if (this.visible) {
                this.renderButton(matrices, mouseX, mouseY, delta);
            }

            this.narrate();
            this.wasHovered = this.isHovered();
        }

        if (this.isMouseOver(mouseX, mouseY)) {
            this.screen.renderTooltip(matrices, new TranslatableText("chesttracker.gui.title"), mouseX, mouseY);
        }
    }

    private void reposition() {
        if (MinecraftClient.getInstance().player != null) {
            // the creative inventory screen is translated when effects exist
             if (screen instanceof CreativeInventoryScreen && !MinecraftClient.getInstance().player.getStatusEffects().isEmpty()) {
                 this.setPos(ButtonPositions.getX(screen, 0) + 60, ButtonPositions.getY(screen, 0));
             } else {
                 this.setPos(ButtonPositions.getX(screen, 0), ButtonPositions.getY(screen, 0));
             }
        }
    }
}
