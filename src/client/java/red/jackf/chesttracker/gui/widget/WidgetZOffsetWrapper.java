package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.mixins.AbstractWidgetAccessor;

/**
 * Wrapper for a widget that renders it with a Z offset on the screen.
 */
public class WidgetZOffsetWrapper<T extends AbstractWidget> extends AbstractWidget {
    private final T baseWidget;
    private final int zOffset;

    public WidgetZOffsetWrapper(T baseWidget, int zOffset) {
        super(baseWidget.getX(), baseWidget.getY(), baseWidget.getWidth(), baseWidget.getHeight(), baseWidget.getMessage());
        this.baseWidget = baseWidget;
        this.zOffset = zOffset;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, zOffset);
        ((AbstractWidgetAccessor) baseWidget).renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().popPose();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        ((AbstractWidgetAccessor) baseWidget).updateWidgetNarration(output);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        baseWidget.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return baseWidget.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return baseWidget.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return baseWidget.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return baseWidget.charTyped(codePoint, modifiers);
    }

    @Nullable
    @Override
    public ComponentPath getCurrentFocusPath() {
        return baseWidget.getCurrentFocusPath();
    }

    @Override
    public void setPosition(int x, int y) {
        baseWidget.setPosition(x, y);
    }
}
