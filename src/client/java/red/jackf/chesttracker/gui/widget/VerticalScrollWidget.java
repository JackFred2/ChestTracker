package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.gui.util.NinePatcher;

import java.util.function.Consumer;

public class VerticalScrollWidget extends AbstractWidget {
    private static final NinePatcher BACKGROUND = new NinePatcher(ChestTracker.guiTex("9patch/scroll_bar"), 2, 5, NinePatcher.InnerMode.STRETCH);
    private static final ResourceLocation HANDLE_TEXTURE = ChestTracker.guiTex("widgets/scroll_bar_handle");
    private static final int HANDLE_WIDTH = 10;
    private static final int HANDLE_HEIGHT = 11;
    private static final int INSET = 1;

    public static final int BAR_WIDTH = 2 * INSET + HANDLE_WIDTH;

    private float progress = 0f;
    private boolean scrolling = false;
    private boolean disabled = false;
    @Nullable
    private Consumer<Float> responder = null;

    public VerticalScrollWidget(int x, int y, int height, Component message) {
        super(x, y, BAR_WIDTH, height, message);
    }

    public void setDisabled(boolean disabled) {
        if (this.disabled != disabled) {
            this.disabled = disabled;
            this.scrolling = false;
        }
    }

    public void setResponder(@Nullable Consumer<Float> responder) {
        this.responder = responder;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        BACKGROUND.draw(graphics, getX(), getY(), width, height);

        int handleY = (int) ((this.height - HANDLE_HEIGHT - 2 * INSET) * progress);
        graphics.blit(HANDLE_TEXTURE,
                this.getX() + INSET,
                this.getY() + INSET + handleY,
                0,
                disabled ? HANDLE_HEIGHT : 0,
                HANDLE_WIDTH,
                HANDLE_HEIGHT,
                HANDLE_WIDTH,
                HANDLE_HEIGHT * 2);
    }

    private boolean isWithinBounds(double x, double y) {
        return x >= this.getX() && x < (this.getX() + getWidth()) && y >= getY() && y < (getY() + getHeight());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.visible && !this.disabled && this.isWithinBounds(mouseX, mouseY) && button == 0) {
            this.scrolling = true;
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.visible && !this.disabled) {
            setProgress((float) (this.progress - delta));
            return true;
        } else {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling && button == 0) {
            var progress = (mouseY - this.getY() - INSET - HANDLE_HEIGHT / 2) / (this.getHeight() - 2 * INSET - HANDLE_HEIGHT);
            setProgress((float) progress);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) this.scrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public void setProgress(float value) {
        this.progress = Mth.clamp(value, 0F, 1F);
        if (this.responder != null) responder.accept(progress);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}