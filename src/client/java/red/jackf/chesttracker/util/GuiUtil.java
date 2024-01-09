package red.jackf.chesttracker.util;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.ChestTracker;

public class GuiUtil {
    public static final ResourceLocation BACKGROUND_SPRITE = sprite("nine_patch/background");
    public static final ResourceLocation SEARCH_BAR_SPRITE = sprite("nine_patch/search_bar");

    public static ResourceLocation sprite(String path) {
        return new ResourceLocation(ChestTracker.ID, path);
    }

    public static ResourceLocation png(String path) {
        return new ResourceLocation(ChestTracker.ID, "textures/gui/sprites/" + path + ".png");
    }

    public static WidgetSprites twoSprite(String path) {
        return new WidgetSprites(sprite("widgets/" + path),
                                 sprite("widgets/" + path + "_highlighted"));
    }

    public static ImageButton close(int x, int y, Button.OnPress callback) {
        var button = new ImageButton(x, y, 12, 12, twoSprite("close/button"), callback);
        button.setTooltip(Tooltip.create(Component.translatable("mco.selectServer.close")));
        return button;
    }
}
