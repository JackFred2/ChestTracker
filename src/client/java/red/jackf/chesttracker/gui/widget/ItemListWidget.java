package red.jackf.chesttracker.gui.widget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.StringUtil;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.client.api.SearchInvoker;
import red.jackf.whereisit.client.api.SearchRequestPopulator;

import java.util.Collections;
import java.util.List;

public class ItemListWidget extends AbstractWidget {
    private static final int UV_X = 0;
    private static final int UV_Y = 44;

    private final int gridWidth;
    private final int gridHeight;
    private List<ItemStack> items = Collections.emptyList();
    private int offset = 0;
    private boolean hideTooltip;

    public ItemListWidget(int x, int y, int gridWidth, int gridHeight) {
        super(x, y, gridWidth * Constants.SLOT_SIZE, gridHeight * Constants.SLOT_SIZE, Component.empty());
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
        int rows = getRows();
        this.offset = Mth.clamp(this.offset, 0, (rows - gridHeight) * gridWidth);
    }

    private List<ItemStack> getOffsetItems() {
        if (this.items.size() == 0) return Collections.emptyList();
        int min = Mth.clamp(this.offset, 0, this.items.size() - 1);
        int max = Mth.clamp(this.offset + gridWidth * gridHeight, 0, this.items.size() - 1);
        return this.items.subList(min, max);
    }

    public int getRows() {
        //noinspection SuspiciousNameCombination
        return Mth.positiveCeilDiv(this.items.size(), this.gridWidth);
    }

    public void onScroll(float progress) {
        int rows = getRows();
        if (rows <= gridHeight) return; // dont do anything
        int range = rows - gridHeight;
        int rowOffset = (int) (progress * (range + 0.5f));
        this.offset = rowOffset * gridWidth;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        ChestTracker.LOGGER.debug(delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        var items = getOffsetItems();
        int x = (int) ((mouseX - getX()) / Constants.SLOT_SIZE);
        int y = (int) ((mouseY - getY()) / Constants.SLOT_SIZE);
        var index = (y * gridWidth) + x;
        if (index >= items.size()) return;
        var request = new SearchRequest();
        SearchRequestPopulator.addItemStack(request, items.get(index), SearchRequestPopulator.Context.inventory());
        SearchInvoker.doSearch(request);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderItems(graphics);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 250.0f);
        this.renderItemDecorations(graphics);
        this.renderAdditional(graphics, mouseX, mouseY);
        graphics.pose().popPose();
    }

    private void renderItemDecorations(GuiGraphics graphics) {
        var items = getOffsetItems();
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            var x = this.getX() + Constants.SLOT_SIZE * (i % gridWidth);
            var y = this.getY() + Constants.SLOT_SIZE * (i / gridWidth);
            graphics.renderItemDecorations(Minecraft.getInstance().font, item, x + 1, y + 1, StringUtil.magnitude(item.getCount(), 0)); // Counts
        }
    }

    private void renderAdditional(GuiGraphics graphics, int mouseX, int mouseY) {
        var items = getOffsetItems();
        if (!this.isHovered()) return;
        var x = (mouseX - getX()) / Constants.SLOT_SIZE;
        var y = (mouseY - getY()) / Constants.SLOT_SIZE;
        if (x < 0 || x > gridWidth || y < 0 || y > gridHeight) return;
        var index = (y * gridWidth) + x;
        if (index >= items.size()) return;
        var slotX = getX() + x * Constants.SLOT_SIZE;
        var slotY = getY() + y * Constants.SLOT_SIZE;
        graphics.fill(slotX + 1, slotY + 1, slotX + Constants.SLOT_SIZE - 1, slotY + Constants.SLOT_SIZE - 1, 0x80_FFFFFF);
        if (!this.hideTooltip) {
            var stack = items.get(index);
            var lines =  Screen.getTooltipFromItem(Minecraft.getInstance(), stack);
            if (stack.getCount() > 999) lines.add(Component.literal(StringUtil.commaSeparated(stack.getCount())).withStyle(ChatFormatting.GREEN));
            var image = stack.getTooltipImage();
            graphics.renderTooltip(Minecraft.getInstance().font, lines, image, mouseX, mouseY);
        }
    }

    private void renderItems(GuiGraphics graphics) {
        var items = getOffsetItems();
        for (int i = 0; i < (gridWidth * gridHeight); i++) {
            var x = this.getX() + Constants.SLOT_SIZE * (i % gridWidth);
            var y = this.getY() + Constants.SLOT_SIZE * (i / gridWidth);
            graphics.blit(Constants.TEXTURE, x, y, UV_X, UV_Y, Constants.SLOT_SIZE, Constants.SLOT_SIZE); // Slot Background
            if (i < items.size()) {
                var item = items.get(i);
                graphics.renderItem(item, x + 1, y + 1); // Item
            }
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }

    public void setHideTooltip(boolean shouldHideTooltip) {
        this.hideTooltip = shouldHideTooltip;
    }
}
