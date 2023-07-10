package red.jackf.chesttracker.gui.widgets;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.Collections;

@Environment(EnvType.CLIENT)
public class WBevelledButton extends WButton {
    private final Text tooltip;
    private boolean pressed = false;
    private boolean highlighted;

    public WBevelledButton(Icon icon, Text tooltip, boolean highlighted) {
        super(icon);
        this.tooltip = tooltip;
        this.highlighted = highlighted;
    }

    @Override
    public void paint(DrawContext matrices, int x, int y, int mouseX, int mouseY) {
        boolean hovered = (mouseX >= 0 && mouseY >= 0 && mouseX < getWidth() && mouseY < getHeight());
        int topLeft;
        int panel;
        int bottomRight;

        if (pressed) {
            topLeft = highlighted ? 0xFF003700 : 0xFF000000;
            panel = highlighted ? (hovered ? 0xFF77CF77 : 0xFF86B686) : (hovered ? 0xFFC6C6C6 : 0xFF969696);
            bottomRight = highlighted ? 0xFFE7FFE7 : 0xFFFFFFFF;
        } else if (isEnabled()) {
            topLeft = highlighted ? 0xFFE7FFE7 : 0xFFFFFFFF;
            panel = highlighted ? (hovered ? 0xFF87E087 : 0xFFA6E6A6) : (hovered ? 0xFF8892C9 : 0xFFC6C6C6);
            bottomRight = highlighted ? (hovered ? 0xFF005700 : 0xFF003700) : (hovered ? 0xFF00073E : 0xFF000000);
        } else {
            topLeft = 0xFFD8D8D8;
            panel = 0xFFC6C6C6;
            bottomRight = 0xFF8F8F8F;
        }

        ScreenDrawing.drawBeveledPanel(matrices, x, y, this.width, this.height, topLeft, panel, bottomRight);

        if (this.getIcon() != null) {
            if (this.getIcon() instanceof ItemIcon) {
                this.getIcon().paint(matrices, x + 1, y + 1, 16);
            } else {
                this.getIcon().paint(matrices, x, y, 16);
            }
        }
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
