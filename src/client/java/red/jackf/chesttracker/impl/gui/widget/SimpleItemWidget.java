package red.jackf.chesttracker.impl.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.world.item.ItemStack;

public record SimpleItemWidget(ItemStack stack, int x, int y, int size) implements Renderable {
    private static final int DEFAULT_ITEM_SIZE = 18;

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        final float factor = (float) this.size / DEFAULT_ITEM_SIZE;

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(factor, factor, factor);
        graphics.renderFakeItem(stack, 0, 0);
        graphics.pose().popPose();
    }
}
