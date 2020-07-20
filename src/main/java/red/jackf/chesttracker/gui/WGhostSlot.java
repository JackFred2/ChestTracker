package red.jackf.chesttracker.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.tracker.Tracker;
import spinnery.widget.WAbstractWidget;
import spinnery.widget.WVerticalScrollableContainer;
import spinnery.widget.api.Position;
import spinnery.widget.api.Size;

import static red.jackf.chesttracker.ChestTracker.id;

public class WGhostSlot extends WAbstractWidget {
    private static final Identifier SLOT_TEX = id("slot.png");
    protected final ItemStack item;
    private final Position basePos;
    private final WVerticalScrollableContainer scrollArea;

    public boolean hover;

    public WGhostSlot(ItemStack item, Position pos, WVerticalScrollableContainer scrollArea) {
        this.item = item;
        this.setSize(Size.of(18, 18));
        this.basePos = pos;
        this.scrollArea = scrollArea;
    }

    @Override
    public void draw(MatrixStack matrices, VertexConsumerProvider provider) {
        super.draw(matrices, provider);
        ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();
        int x = (int) this.getX();
        int y = (int) this.getY();
        MinecraftClient.getInstance().getTextureManager().bindTexture(SLOT_TEX);
        DrawableHelper.drawTexture(matrices, x, y, 0, 0, 18, 18, 18, 18);
        x += 1;
        y += 1;
        renderer.renderInGui(item, x, y);
        renderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, item, x, y, getCountString());

        if (hover) {
            matrices.translate(0, 0, 200);
            DrawableHelper.fill(matrices, x, y, x + 16, y + 16, 0x5affffff);
            matrices.translate(0, 0, -200);
        }
    }

    @Nullable
    private String getCountString() {
        int count = item.getCount();
        if (count < 1_000) return null;
        if (count < 1_000_000) return "" + Math.floorDiv(count, 1_000) + "k";
        if (count < 1_000_000_000) return "" + Math.floorDiv(count, 1_000_000) + "M";
        else return "" + Math.floorDiv(count, 1_000_000_000) + "G";
    }

    @Override
    public void onMouseClicked(float mouseX, float mouseY, int mouseButton) {
        if (this.isWithinBounds(mouseX, mouseY) && this.scrollArea.isWithinBounds(mouseX, mouseY) && !this.isHidden())
            Tracker.getInstance().searchForItem(item);
    }

    public void updatePos(float rootX, float rootY, float rootZ) {
        this.setPosition(basePos.add(rootX, rootY, rootZ));
    }
}
