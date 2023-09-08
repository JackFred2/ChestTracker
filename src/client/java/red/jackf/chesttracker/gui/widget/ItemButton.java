package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.ChestTracker;

public class ItemButton extends Button {
    public static final int SIZE = 20;
    private static final ResourceLocation TEXTURE = ChestTracker.guiTex("widgets/memory_key_background");
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
                var texY = (this.highlighted || this.isHovered()) ? SIZE : 0;
                graphics.blit(TEXTURE, getX(), getY(), 0, texY, SIZE, SIZE, SIZE, SIZE * 2);
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
