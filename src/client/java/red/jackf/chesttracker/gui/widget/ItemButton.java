package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.util.Constants;

public class ItemButton extends Button {
    private static final int SIZE = 20;
    private static final int CUSTOM_BACKGROUND_UV_X = 0;
    private static final int CUSTOM_BACKGROUND_UV_Y = 153;
    private final ItemStack stack;
    private final Component tooltip;
    private final boolean shouldShowBackground;
    private final int tooltipZMod;
    private final boolean useChestTrackerBackground;
    private boolean highlighted = false;

    public ItemButton(ItemStack stack, int x, int y, Component tooltip, OnPress onPress, boolean shouldShowBackground, int tooltipZMod, boolean useChestTrackerBackground) {
        super(x, y, SIZE, SIZE, CommonComponents.EMPTY, onPress, Button.DEFAULT_NARRATION);
        this.tooltip = tooltip;
        this.stack = stack;
        this.shouldShowBackground = shouldShowBackground;
        this.tooltipZMod = tooltipZMod;
        this.useChestTrackerBackground = useChestTrackerBackground;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.shouldShowBackground) {
            if (useChestTrackerBackground) {
                var texY = CUSTOM_BACKGROUND_UV_Y;
                if (this.highlighted || this.isHovered()) texY += SIZE;
                graphics.blit(Constants.TEXTURE, getX(), getY(), CUSTOM_BACKGROUND_UV_X, texY, SIZE, SIZE);
            } else {
                super.renderWidget(graphics, mouseX, mouseY, partialTick);
            }
        }
        graphics.renderItem(stack, this.getX() + 2, this.getY() + 2);
        if (this.isHovered || this.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard()) {
            if (tooltipZMod != 0) {
                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, tooltipZMod);
            }
            graphics.renderTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
            if (tooltipZMod != 0) graphics.pose().popPose();
        }
    }

    @Override
    public void renderString(@NotNull GuiGraphics graphics, @NotNull Font font, int color) {
        // noop
    }
}
