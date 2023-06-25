package red.jackf.chesttracker.gui.widgets;

import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import red.jackf.chesttracker.util.DarkModeIcon;

import java.util.Collections;

import static red.jackf.chesttracker.ChestTracker.id;

public class WPageButton extends WButton {
    private static final DarkModeIcon LEFT_BUTTON = DarkModeIcon.fromFolder("left_button.png");
    private static final DarkModeIcon RIGHT_BUTTON = DarkModeIcon.fromFolder("right_button.png");
    private static final DarkModeIcon LEFT_BUTTON_DISABLED = DarkModeIcon.fromFolder("left_button_disabled.png");
    private static final DarkModeIcon RIGHT_BUTTON_DISABLED = DarkModeIcon.fromFolder("right_button_disabled.png");
    private static final DarkModeIcon LEFT_BUTTON_HIGHLIGHT = DarkModeIcon.fromFolder("left_button_highlight.png");
    private static final DarkModeIcon RIGHT_BUTTON_HIGHLIGHT = DarkModeIcon.fromFolder("right_button_highlight.png");

    private final Text tooltip;
    private final boolean isPrevious;
    private boolean pressed = false;
    private boolean highlighted;

    public WPageButton(boolean isLeft, Text tooltip, boolean highlighted) {
        super();
        this.isPrevious = isLeft;
        this.tooltip = tooltip;
        this.highlighted = highlighted;
    }

    @Override
    public void paint(DrawContext matrices, int x, int y, int mouseX, int mouseY) {
        boolean hovered = (mouseX >= 0 && mouseY >= 0 && mouseX < getWidth() && mouseY < getHeight());
        Icon icon;
        if (!this.isEnabled()) icon = (isPrevious ? LEFT_BUTTON_DISABLED.get() : RIGHT_BUTTON_DISABLED.get());
        else if (hovered) icon = (isPrevious ? LEFT_BUTTON_HIGHLIGHT.get() : RIGHT_BUTTON_HIGHLIGHT.get());
        else icon = (isPrevious ? LEFT_BUTTON.get() : RIGHT_BUTTON.get());

        icon.paint(matrices, x, y, 16);
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    @Override
    public InputResult onClick(int x, int y, int button) {
        if (!pressed && isEnabled() && isWithinBounds(x, y) && getOnClick() != null) {
            getOnClick().run();
            return InputResult.PROCESSED;
        }
        return InputResult.IGNORED;
    }

    @Override
    public void renderTooltip(DrawContext matrices, int x, int y, int tX, int tY) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        var client = MinecraftClient.getInstance();
        if (screen != null)
            matrices.drawTooltip(client.textRenderer, Collections.singletonList(tooltip.asOrderedText()), HoveredTooltipPositioner.INSTANCE, tX + x, tY + y);
    }

    @Override
    public void setSize(int x, int y) {
        this.width = x;
        this.height = y;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }
}
