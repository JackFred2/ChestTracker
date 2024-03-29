package red.jackf.chesttracker.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.gui.util.SpriteSet;
import red.jackf.chesttracker.gui.widget.SpriteButton;

public class GuiUtil {
    public static final NinePatch BACKGROUND_SPRITE = new GuiUtil.NinePatch("nine_patch/background_1.20.1", 6, 30, 30);
    public static final NinePatch SEARCH_BAR_SPRITE = new GuiUtil.NinePatch("nine_patch/search_bar_1.20.1", 2, 12, 12);

    public static ResourceLocation sprite(String path) {
        return new ResourceLocation(ChestTracker.ID, path);
    }

    public static ResourceLocation png(String path) {
        return new ResourceLocation(ChestTracker.ID, "textures/gui/sprites/" + path + ".png");
    }

    public static SpriteSet twoSprite(String path) {
        return new SpriteSet(png("widgets/" + path),
                png("widgets/" + path + "_highlighted"));
    }

    public static SpriteButton close(int x, int y, Button.OnPress callback) {
        var button = new SpriteButton(x, y, 12, 12, twoSprite("close/button"), callback);
        button.setTooltip(Tooltip.create(Component.translatable("mco.selectServer.close")));
        return button;
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int z, int width, int height) {
        graphics.blit(texture,
                x,
                y,
                z,
                0,
                0,
                width,
                height,
                width,
                height);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height) {
        blit(graphics, texture, x, y, 0, width, height);
    }

    public static void blitRepeating(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height, int texWidth, int texHeight) {
        graphics.innerBlit(texture, x, x + width, y, y + height, 0, 0f, (float) width / texWidth, 0f, (float) height / texHeight);
    }

    public record NinePatch(ResourceLocation texture, int border, int textureWidth, int textureHeight) {
        public NinePatch(String path, int border, int textureWidth, int textureHeight) {
            this(png(path), border, textureWidth, textureHeight);
        }

        public void blit(GuiGraphics graphics, int x, int y, int width, int height) {
            graphics.blitNineSliced(this.texture, x, y, width, height, this.border, this.textureWidth, this.textureHeight, 0, 0);
        }
    }
}
