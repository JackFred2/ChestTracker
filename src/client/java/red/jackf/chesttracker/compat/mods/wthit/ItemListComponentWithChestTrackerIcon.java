package red.jackf.chesttracker.compat.mods.wthit;

import mcp.mobius.waila.api.ITooltipComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.ChestTracker;

import java.util.List;

/**
 * Copy of ItemListComponent, with Chest Tracker's icon on the left to notify players..
 */
public class ItemListComponentWithChestTrackerIcon implements ITooltipComponent.HorizontalGrowing {
    private static final int LEFT_PAD = 18;
    private static final ResourceLocation ICON = ChestTracker.id("icon.png");

    public ItemListComponentWithChestTrackerIcon(List<ItemStack> items, int maxHeight) {
        this.items = items;
        this.maxHeight = maxHeight;
    }

    private final List<ItemStack> items;
    private final int maxHeight;

    private int gridWidth;
    private int gridHeight;
    private int maxIndex;

    @Override
    public int getMinimalWidth() {
        return Math.min(items.size(), 9) * 18 + LEFT_PAD;
    }

    @Override
    public void setGrownWidth(int grownWidth) {
        gridWidth = (grownWidth - LEFT_PAD) / 18;
        gridHeight = items.isEmpty() ? 0 : Math.min(Mth.positiveCeilDiv(items.size(), gridWidth), maxHeight);
        maxIndex = gridWidth * gridHeight - 1;
    }

    @Override
    public int getHeight() {
        return gridHeight * 18;
    }

    @Override
    public void render(GuiGraphics ctx, int x, int y, float delta) {
        for (var i = 0; i < items.size(); i++) {
            var item = items.get(i);
            var ix = x + LEFT_PAD + (18 * (i % gridWidth)) + 1;
            var iy = y + (18 * (i / gridWidth)) + 1;
            ctx.renderItem(item, ix, iy);
            ctx.renderItemDecorations(Minecraft.getInstance().font, item, ix, iy);

            if (i == maxIndex) break;
        }
        int rows = Mth.clamp(Mth.positiveCeilDiv(items.size(), gridWidth), 1, maxHeight) - 1;
        ctx.blit(ICON, x + 1, y + 1 + 9 * rows, 0, 0, 0, 16, 16, 16, 16);
    }
}
