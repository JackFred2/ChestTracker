package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.util.GuiUtil;

import java.util.function.Consumer;

/**
 * Handle for reordering memory key icons in the edit keys screen.
 */
public class DragHandleWidget extends AbstractWidget {
    private static final WidgetSprites TEXTURE = GuiUtil.twoSprite("drag_handle/handle");
    private static final int HIGHLIGHT_COLOUR = 0xFF_FF0000;
    public static final int WIDTH = 10;
    public static final int HEIGHT = 20;
    private final int highlightStartX;
    private final int highlightStartY;
    private final int highlightWidth;
    private final int yHeight;
    private final int minIndex;
    private final int maxIndex;
    private final Consumer<@Nullable Integer> callback;

    private @Nullable Integer target = null;

    public DragHandleWidget(int x, int y, int highlightStartX, int highlightStartY, int highlightWidth, int yHeight, int minIndex, int maxIndex, Consumer<Integer> callback) {
        super(x, y, WIDTH, HEIGHT, Component.empty());
        this.highlightStartX = highlightStartX;
        this.highlightStartY = highlightStartY;
        this.highlightWidth = highlightWidth;
        this.yHeight = yHeight;
        this.minIndex = minIndex;
        this.maxIndex = maxIndex;
        this.callback = callback;

        this.setTooltip(Tooltip.create(Component.translatable("chesttracker.gui.reorder")));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blitSprite(this.isHoveredOrFocused() ? TEXTURE.enabledFocused() : TEXTURE.enabled(), this.getX(), this.getY(), WIDTH, HEIGHT);

        if (this.target != null) {
            int y = this.highlightStartY + yHeight * this.target;
            graphics.fill(this.highlightStartX, y, this.highlightStartX + highlightWidth, y + 1, HIGHLIGHT_COLOUR);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, Component.translatable("chesttracker.gui.reorder"));
        narration.add(NarratedElementType.USAGE, Component.translatable("chesttracker.gui.narration.drag"));
    }

    private void updateTarget(double mouseY) {
        this.target = Mth.clamp((int) ((mouseY - this.highlightStartY + yHeight / 2) / yHeight), minIndex, maxIndex);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        this.updateTarget(mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.target != null) {
            this.callback.accept(this.target);
            this.target = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);
        if (this.target != null) this.updateTarget(mouseY);
    }
}
