package red.jackf.chesttracker.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
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

import java.util.Collection;

import static red.jackf.chesttracker.ChestTracker.id;

@Environment(EnvType.CLIENT)
public class ChestTrackerButtonWidget extends TexturedButtonWidget {
    private static final Identifier TEXTURE_MAIN = id("textures/gui_button_small.png");
    private static final Identifier TEXTURE_FORGET = id("textures/forget_button.png");
    private static final Identifier TEXTURE_REMEMBER = id("textures/remember_button.png");
    private static final Identifier BACKGROUND_TEXTURE = id("textures/gui_button_background.png");
    private static final Identifier NAME_EDIT_TEXTURE = id("textures/text_button.png");
    private final HandledScreen<?> screen;
    private final boolean forgetOrRememberEnabled;
    private boolean isRemembered = false;

    public ChestTrackerButtonWidget(HandledScreen<?> screen, boolean forgetOrRememberEnabled) {
        super(0, 0, 9, 9, 0, 0, 9, TEXTURE_MAIN, 9, 18, (buttonRaw) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            ChestTrackerButtonWidget button = (ChestTrackerButtonWidget) buttonRaw;
            if (button.shouldShowAltButton()) {
                MemoryDatabase database = MemoryDatabase.getCurrent();
                BlockPos pos = MemoryUtils.getLatestPos();
                if (database != null && client.world != null && pos != null) {
                    if (button.shouldShowRememberButton()) {
                        ChestTracker.sendDebugMessage(new TranslatableText("chesttracker.remembered_new_location"));
                        MemoryUtils.setForceNextMerge(true);
                    } else {
                        if (MemoryUtils.wasLastEnderchest()) {
                            database.removePos(MemoryUtils.ENDER_CHEST_ID, BlockPos.ORIGIN);
                            ChestTracker.sendDebugMessage(new TranslatableText("chesttracker.forgot_ender_chest"));
                        } else {
                            Collection<BlockPos> connected = MemoryUtils.getConnected(client.world, pos);
                            connected.forEach(connectedPos -> database.removePos(client.world.getRegistryKey().getValue(), connectedPos));
                            database.removePos(client.world.getRegistryKey().getValue(), pos);
                            ChestTracker.sendDebugMessage(new TranslatableText("chesttracker.forgot_location", pos.getX(), pos.getY(), pos.getZ()));
                        }
                        MemoryUtils.ignoreNextMerge();
                    }
                    screen.onClose();
                }
            } else {
                screen.onClose();
                client.setScreen(new ItemListScreen());
            }
        });
        this.screen = screen;
        this.forgetOrRememberEnabled = forgetOrRememberEnabled;
        MemoryDatabase database = MemoryDatabase.getCurrent();
        MinecraftClient client = MinecraftClient.getInstance();
        if (database != null && client.world != null && MemoryUtils.getLatestPos() != null)
            this.isRemembered = database.positionExists(client.world.getRegistryKey().getValue(), MemoryUtils.getLatestPos());
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
            if (shouldShowAltButton()) {
                this.screen.renderTooltip(matrices, shouldShowRememberButton() ? new TranslatableText("chesttracker.gui.remember_location") : new TranslatableText("chesttracker.gui.delete_location"), mouseX, mouseY);
            } else {
                this.screen.renderTooltip(matrices, new TranslatableText("chesttracker.gui.title"), mouseX, mouseY);
            }
        }
    }

    private boolean shouldShowAltButton() {
        return Screen.hasShiftDown() && forgetOrRememberEnabled;
    }

    private boolean shouldShowRememberButton() {
        return !isRemembered && !ChestTracker.CONFIG.miscOptions.rememberNewChests && !MemoryUtils.wasLastEnderchest();
    }

    private void reposition() {
        if (MinecraftClient.getInstance().player != null) {
            // the creative inventory screen is translated when effects exist
            // seems to be fixed in 1.18
            /*if (screen instanceof CreativeInventoryScreen && !MinecraftClient.getInstance().player.getStatusEffects().isEmpty()) {
                this.setPos(ButtonPositions.getX(screen, 0) + 60, ButtonPositions.getY(screen, 0));
            } else {
                this.setPos(ButtonPositions.getX(screen, 0), ButtonPositions.getY(screen, 0));
            }*/
            this.setPos(ButtonPositions.getX(screen, 0), ButtonPositions.getY(screen, 0));
        }
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        if (shouldShowAltButton()) {
            RenderSystem.setShaderTexture(0, shouldShowRememberButton() ? TEXTURE_REMEMBER : TEXTURE_FORGET);
        } else {
            RenderSystem.setShaderTexture(0, TEXTURE_MAIN);
        }
        int offset = 0;
        if (this.isHovered()) {
            offset = 9;
        }

        RenderSystem.enableDepthTest();
        drawTexture(matrices, this.x, this.y, 0, offset, this.width, this.height, 9, 18);
        if (this.isHovered()) {
            this.renderTooltip(matrices, mouseX, mouseY);
        }

    }
}
