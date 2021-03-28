package red.jackf.chesttracker.gui.widgets;

import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.Collections;

import static red.jackf.chesttracker.ChestTracker.id;

public class WPageButton extends WButton {
    private static final Icon LEFT_BUTTON = new TextureIcon(id("textures/left_button.png"));
    private static final Icon RIGHT_BUTTON = new TextureIcon(id("textures/right_button.png"));
    private static final Icon LEFT_BUTTON_DISABLED = new TextureIcon(id("textures/left_button_disabled.png"));
    private static final Icon RIGHT_BUTTON_DISABLED = new TextureIcon(id("textures/right_button_disabled.png"));
    private static final Icon LEFT_BUTTON_HIGHLIGHT = new TextureIcon(id("textures/left_button_highlight.png"));
    private static final Icon RIGHT_BUTTON_HIGHLIGHT = new TextureIcon(id("textures/right_button_highlight.png"));

    private final Text tooltip;
    private boolean pressed = false;
    private boolean highlighted;
    private final boolean isPrevious;

    public WPageButton(boolean isLeft, Text tooltip, boolean highlighted) {
        super();
        this.isPrevious = isLeft;
        this.tooltip = tooltip;
        this.highlighted = highlighted;
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        boolean hovered = (mouseX >= 0 && mouseY >= 0 && mouseX < getWidth() && mouseY < getHeight());
        Icon icon;
        if (!this.isEnabled()) icon = (isPrevious ? LEFT_BUTTON_DISABLED : RIGHT_BUTTON_DISABLED);
        else if (hovered) icon = (isPrevious ? LEFT_BUTTON_HIGHLIGHT : RIGHT_BUTTON_HIGHLIGHT);
        else icon = (isPrevious ? LEFT_BUTTON : RIGHT_BUTTON);

        icon.paint(matrices, x, y, 16);
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    @Override
    public void onClick(int x, int y, int button) {
        if (!pressed && isEnabled() && isWithinBounds(x, y))
            if (getOnClick() != null) getOnClick().run();
    }

    @Override
    public void renderTooltip(MatrixStack matrices, int x, int y, int tX, int tY) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen != null)
            screen.renderOrderedTooltip(matrices, Collections.singletonList(tooltip.asOrderedText()), tX + x, tY + y);
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
