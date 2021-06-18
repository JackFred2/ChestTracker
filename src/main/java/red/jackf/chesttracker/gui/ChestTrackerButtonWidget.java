package red.jackf.chesttracker.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.memory.MemoryDatabase;
import red.jackf.chesttracker.memory.MemoryUtils;

import static red.jackf.chesttracker.ChestTracker.id;

@Environment(EnvType.CLIENT)
public class ChestTrackerButtonWidget extends TexturedButtonWidget {
    private static final Identifier TEXTURE_MAIN = id("textures/gui_button_small.png");
    private static final Identifier TEXTURE_FORGET = id("textures/forget_button.png");
    private static final Identifier BACKGROUND_TEXTURE = id("textures/gui_button_background.png");
    private static final Identifier NAME_EDIT_TEXTURE = id("textures/text_button.png");
    private final HandledScreen<?> screen;
    private final boolean deleteEnabled;

    public ChestTrackerButtonWidget(HandledScreen<?> screen, boolean deleteEnabled) {
        super(0, 0, 9, 9, 0, 0, 9, TEXTURE_MAIN, 9, 18, (button) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (Screen.hasShiftDown() && deleteEnabled) {
                MemoryDatabase database = MemoryDatabase.getCurrent();
                BlockPos pos = MemoryUtils.getLatestPos();
                if (database != null && client.world != null && pos != null) {
                    if (MemoryUtils.wasLastEnderchest()) {
                        database.removePos(MemoryUtils.ENDER_CHEST_ID, BlockPos.ORIGIN);
                        ChestTracker.sendDebugMessage(new TranslatableText("chesttracker.forgot_ender_chest"));
                    } else {
                        database.removePos(client.world.getRegistryKey().getValue(), pos);
                        ChestTracker.sendDebugMessage(new TranslatableText("chesttracker.forgot_location", pos.getX(), pos.getY(), pos.getZ()));
                    }
                    MemoryUtils.ignoreNextMerge();
                    screen.onClose();
                }
            } else {
                screen.onClose();
                client.openScreen(new ItemListScreen());
            }
        });
        this.screen = screen;
        this.deleteEnabled = deleteEnabled;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.reposition();
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

        // render
        if (this.visible) {
            renderButton(matrices, mouseX, mouseY, delta);
        }

        if (this.isMouseOver(mouseX, mouseY)) {
            this.screen.renderTooltip(matrices, (Screen.hasShiftDown() && deleteEnabled) ? new TranslatableText("chesttracker.gui.delete_location") : new TranslatableText("chesttracker.gui.title"), mouseX, mouseY);
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

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, (Screen.hasShiftDown() && deleteEnabled) ? TEXTURE_FORGET : TEXTURE_MAIN);
        int offset = 0;
        if (this.isHovered()) {
            offset = 9;
        }

        RenderSystem.enableDepthTest();
        drawTexture(matrices, this.x, this.y, 0, offset, this.width, this.height, 9, 18);
        if (this.isHovered()) {
            this.renderToolTip(matrices, mouseX, mouseY);
        }

    }
}
