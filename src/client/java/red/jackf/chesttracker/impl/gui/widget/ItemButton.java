package red.jackf.chesttracker.impl.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.impl.util.GuiUtil;
import red.jackf.chesttracker.impl.gui.util.SpriteSet;

public class ItemButton extends Button {
    public static final int SIZE = 20;
    private static final SpriteSet TEXTURE = GuiUtil.twoSprite("memory_key_background/background");
    private final ItemStack stack;
    private final Background background;
    private boolean highlighted = false;

    public ItemButton(ItemStack stack, int x, int y, OnPress onPress, Background background) {
        super(x, y, SIZE, SIZE, CommonComponents.EMPTY, onPress, Button.DEFAULT_NARRATION);
        this.stack = stack;
        this.background = background;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        switch (background) {
            case VANILLA -> super.renderWidget(graphics, mouseX, mouseY, partialTick);
            case CUSTOM -> GuiUtil.blit(graphics,
                                        this.highlighted || this.isHovered() ? TEXTURE.focused() : TEXTURE.enabled(),
                                        getX(),
                                        getY(),
                                        SIZE,
                                        SIZE);
        }
        graphics.renderItem(stack, this.getX() + 2, this.getY() + 2);
    }

    @Override
    public void renderString(@NotNull GuiGraphics graphics, @NotNull Font font, int color) {
        // noop
    }

    public enum Background {
        NONE,
        VANILLA,
        CUSTOM
    }
}
