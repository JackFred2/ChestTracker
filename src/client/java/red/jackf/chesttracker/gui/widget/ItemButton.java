package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemButton extends Button {
    private static final int SIZE = 20;
    private final ItemStack stack;
    private boolean shouldShowBackground = false;

    public ItemButton(ItemStack stack, int x, int y, Component tooltip, OnPress onPress) {
        super(x, y, SIZE, SIZE, CommonComponents.EMPTY, onPress, Button.DEFAULT_NARRATION);
        this.setTooltip(Tooltip.create(tooltip));
        this.stack = stack;
    }

    public void showBackground(boolean shouldShowBackground) {
        this.shouldShowBackground = shouldShowBackground;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.shouldShowBackground) super.renderWidget(graphics, mouseX, mouseY, partialTick);
        graphics.renderItem(stack, this.getX() + 2, this.getY() + 2);
    }

    @Override
    public void renderString(@NotNull GuiGraphics graphics, @NotNull Font font, int color) {
        // noop
    }
}
