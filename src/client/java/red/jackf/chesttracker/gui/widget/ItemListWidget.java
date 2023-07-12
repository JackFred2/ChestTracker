package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.util.Constants;

import java.util.Collections;
import java.util.List;

public class ItemListWidget extends AbstractWidget {
    private static final int UV_X = 0;
    private static final int UV_Y = 28;

    private final int gridWidth;
    private final int gridHeight;
    private List<ItemStack> items = Collections.emptyList();
    public ItemListWidget(int x, int y, int gridWidth, int gridHeight) {
        super(x, y, gridWidth * Constants.SLOT_SIZE, gridHeight * Constants.SLOT_SIZE, Component.empty());
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
            var x = this.getX() + Constants.SLOT_SIZE * (i % gridWidth);
            var y = this.getY() + Constants.SLOT_SIZE * (i / gridWidth);
            graphics.blit(Constants.TEXTURE, x - 1, y - 1, UV_X, UV_Y, Constants.SLOT_SIZE, Constants.SLOT_SIZE); // Slot Background
            graphics.renderItem(item, x, y); // Item
            graphics.renderItemDecorations(Minecraft.getInstance().font, item, x, y); // Text
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
