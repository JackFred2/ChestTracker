package red.jackf.chesttracker.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import static red.jackf.chesttracker.ChestTracker.id;

@Environment(EnvType.CLIENT)
public class ChestTrackerButtonsWidget extends TexturedButtonWidget {
    private static final Identifier MAIN_TEXTURE = id("textures/gui_button_small.png");
    private static final Identifier BACKGROUND_TEXTURE = id("textures/gui_button_background.png");
    private static final Identifier NAME_EDIT_TEXTURE = id("textures/text_button.png");
    private static final int RENAME_BUTTON_X_MAX = 8;
    private static final int MAIN_BUTTON_X_MIN = 13;

    private final HandledScreen<?> screen;
    private boolean wasHovered = false;
    private int lastMouseX = 0;

    public ChestTrackerButtonsWidget(HandledScreen<?> screen) {
        super(0, 0, 9, 9, 0, 0, 22, MAIN_TEXTURE, 9, 44, (button) -> {
            if (!(button instanceof ChestTrackerButtonsWidget)) return;
            int lastMouseX = ((ChestTrackerButtonsWidget) button).lastMouseX;
            MinecraftClient client = MinecraftClient.getInstance();
            if (lastMouseX <= RENAME_BUTTON_X_MAX) {
                System.out.println("Rename");
            } else if (lastMouseX >= MAIN_BUTTON_X_MIN) {
                if (client.currentScreen != null) client.currentScreen.onClose();
                client.openScreen(new ItemListScreen());
            }
        });
        this.screen = screen;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.reposition();
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

        if (this.wasHovered != this.isHovered()) {
            if (this.isHovered()) {
                this.x = this.x - 13;
            } else {
                this.x = this.x + 13;
            }
        }

        if (this.isHovered()) {
            this.width = 22;
            MinecraftClient.getInstance().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
            drawTexture(matrices, this.x - 3, this.y - 3, 0, 0, 28, 15, 28, 15);
            MinecraftClient.getInstance().getTextureManager().bindTexture(MAIN_TEXTURE);
            drawTexture(matrices, this.x + 13, this.y, 0, 0, 9, 9, 9, 18);
        } else {
            this.width = 9;
        }

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
            int xPos = mouseX - this.x;

            if (xPos <= RENAME_BUTTON_X_MAX) {
                this.screen.renderTooltip(matrices, new TranslatableText("chesttracker.gui.rename"), mouseX, mouseY);
            } else if (xPos >= MAIN_BUTTON_X_MIN) {
                this.screen.renderTooltip(matrices, new TranslatableText("chesttracker.gui.title"), mouseX, mouseY);
            }
            this.lastMouseX = xPos;
        }
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.getTextureManager().bindTexture(this.isHovered() ? NAME_EDIT_TEXTURE : MAIN_TEXTURE);

        RenderSystem.enableDepthTest();
        drawTexture(matrices, this.x, this.y, 0, 0, 9, 9, 9, 18);
        if (this.isHovered()) {
            this.renderToolTip(matrices, mouseX, mouseY);
        }
    }

    private void reposition() {
        this.setPos(ButtonPositions.getX(screen, 0) + (this.isHovered() ? - 13 : 0), ButtonPositions.getY(screen, 0));
    }
}
