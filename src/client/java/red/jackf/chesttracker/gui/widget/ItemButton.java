package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemButton extends Button {
    private final ItemStack stack;

    public ItemButton(ItemStack stack, int x, int y, Component tooltip, OnPress onPress) {
        super(x, y, 16, 16, CommonComponents.EMPTY, onPress, Button.DEFAULT_NARRATION);
        this.setTooltip(Tooltip.create(tooltip));
        this.stack = stack;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.renderItem(stack, this.getX(), this.getY());
    }
}
