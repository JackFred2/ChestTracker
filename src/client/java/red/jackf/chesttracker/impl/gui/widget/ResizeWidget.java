package red.jackf.chesttracker.impl.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.impl.util.GuiUtil;

import java.util.function.BiConsumer;

public class ResizeWidget extends AbstractWidget {
    private static final ResourceLocation TEXTURE = GuiUtil.png("widgets/resize");
    private static final int SIZE = 10; // px
    private final int stepSize;
    private final int currentWidth;
    private final int currentHeight;
    private final int minWidth;
    private final int minHeight;
    private final int maxWidth;
    private final int maxHeight;
    private final BiConsumer<Integer, Integer> callback;
    private final int left;
    private final int top;
    @Nullable
    private Pair<Integer, Integer> target = null;

    public ResizeWidget(int x, int y, int screenLeft, int screenTop, int stepSize, int currentWidth, int currentHeight, int minWidth, int minHeight, int maxWidth, int maxHeight, BiConsumer<Integer, Integer> callback) {
        super(x, y, SIZE, SIZE, Component.empty());
        this.left = screenLeft;
        this.top = screenTop;
        if (currentWidth < minWidth || currentWidth > maxWidth)
            throw new IllegalArgumentException("Resize width out of bounds: %d".formatted(currentWidth));
        if (currentHeight < minHeight || currentHeight > maxHeight)
            throw new IllegalArgumentException("Resize height out of bounds: %d".formatted(currentHeight));
        this.stepSize = stepSize;
        this.currentWidth = currentWidth;
        this.currentHeight = currentHeight;
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.callback = callback;

        this.setTooltip(Tooltip.create(Component.translatable("chesttracker.gui.resize")));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(TEXTURE, this.getX(), this.getY(), 0, 0, SIZE, SIZE, SIZE, SIZE);

        // border
        if (this.target != null) {
            var right = this.getX() + (this.target.getLeft() - this.currentWidth) * stepSize + 10;
            var bottom = this.getY() + (this.target.getRight() - this.currentHeight) * stepSize + 10;

            graphics.fill(left - 1, top - 1, right + 1, top, 0xFF_FF0000);
            graphics.fill(left - 1, bottom, right + 1, bottom + 1, 0xFF_FF0000);
            graphics.fill(left - 1, top, left, bottom, 0xFF_FF0000);
            graphics.fill(right, top, right + 1, bottom, 0xFF_FF0000);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("chesttracker.gui.resize"));
        narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("chesttracker.gui.narration.drag"));
    }

    private void updateTarget(double mouseX, double mouseY) {
        this.target = Pair.of(
                Mth.clamp((int) (currentWidth + (mouseX - this.getX()) / stepSize), minWidth, maxWidth),
                Mth.clamp((int) (currentHeight + (mouseY - this.getY()) / stepSize), minHeight, maxHeight)
        );
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        updateTarget(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.target != null) {
            if (this.target.getLeft() == currentWidth && this.target.getRight() == currentHeight) {
                this.target = null;
            } else {
                this.callback.accept(this.target.getLeft(), this.target.getRight());
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);
        if (this.target != null) updateTarget(mouseX, mouseY);
    }
}
