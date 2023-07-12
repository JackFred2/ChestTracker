package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ItemListWidget extends AbstractWidget {
    public static final int SLOT_SIZE = 18;

    private final int gridWidth;
    private final int gridHeight;
    private List<ItemStack> items = Collections.emptyList();
    public ItemListWidget(int x, int y, int gridWidth, int gridHeight) {
        super(x, y, gridWidth * SLOT_SIZE, gridHeight * SLOT_SIZE, Component.empty());
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderItems(graphics);
    }

    private void renderItems(GuiGraphics graphics) {
        for (int i = 0; i < this.items.size() && i < (gridWidth * gridHeight); i++) {
            var item = this.items.get(i);
            var x = this.getX() + SLOT_SIZE * (i % gridWidth);
            var y = this.getY() + SLOT_SIZE * (i / gridWidth);
            graphics.renderItem(item, x, y);

            graphics.renderItemDecorations(Minecraft.getInstance().font, item, x, y);
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
