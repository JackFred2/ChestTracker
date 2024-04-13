package red.jackf.chesttracker.impl.gui.invbutton.ui;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

/**
 * Secondary feature button. Only appears when the primary Chest Tracker button is hovered.
 */
public class SecondaryButton extends AbstractWidget {
    private static final long TWEEN_TIME = 100;
    private final WidgetSprites sprites;
    protected Runnable onClick;
    private long startTweenTime = -1;
    private int startX = 0;
    private int startY = 0;
    private int buttonIndex = 0;

    public SecondaryButton(WidgetSprites sprites, Component message, Runnable onClick) {
        super(0, 0, InventoryButton.SIZE, InventoryButton.SIZE, message); // pos updated in InventoryButton#applyPosition
        this.onClick = onClick;
        this.setTooltip(Tooltip.create(message));
        this.sprites = sprites;
        this.visible = false;
    }

    // used to delay tweening and control z order
    protected void setButtonIndex(int index) {
        this.buttonIndex = index;
    }

    protected WidgetSprites getSprites() {
        return this.sprites;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture = getSprites().get(this.isActive(), this.isHoveredOrFocused());

        long tweenTime = this.buttonIndex * TWEEN_TIME;
        float factor = Mth.clamp((float) (Util.getMillis() - startTweenTime) / tweenTime, 0, 1);
        int x = Mth.lerpDiscrete(factor, this.startX - 1, this.getX() - 1);
        int y = Mth.lerpDiscrete(factor, this.startY - 1, this.getY() - 1);

        graphics.blitSprite(texture, x, y, InventoryButton.Z_OFFSET - 10 * buttonIndex, InventoryButton.IMAGE_SIZE, InventoryButton.IMAGE_SIZE);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        onClick.run();
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }

    public void setVisible(boolean shouldShow, int startX, int startY) {
        this.visible = shouldShow;
        if (shouldShow) {
            if (this.startTweenTime == -1) {
                this.startTweenTime = Util.getMillis();
                this.startX = startX;
                this.startY = startY;
            }
        } else {
            this.startTweenTime = -1;
        }
    }
}
