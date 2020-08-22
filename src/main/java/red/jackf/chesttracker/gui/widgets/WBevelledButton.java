package red.jackf.chesttracker.gui.widgets;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.Collections;

public class WBevelledButton extends WButton {
    private final Text tooltip;
    private int iconSize = 16;

    public WBevelledButton(Icon icon, Text tooltip) {
        super(icon);
        this.tooltip = tooltip;
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        boolean hovered = (mouseX>=0 && mouseY>=0 && mouseX<getWidth() && mouseY<getHeight());
        int topLeft;
        int panel;
        int bottomRight;

        if (isEnabled()) {
            topLeft = 0xFFFFFFFF;
            panel = hovered ? 0xFF8892C9 : 0xFFC6C6C6;
            bottomRight = hovered ? 0xFF00073E : 0xFF000000;
        } else {
            topLeft = 0xFFD8D8D8;
            panel = 0xFFC6C6C6;
            bottomRight = 0xFF8F8F8F;
        }

        ScreenDrawing.drawBeveledPanel(x, y, this.width, this.height, topLeft, panel, bottomRight);

        if (this.getIcon() != null) {
            if (this.getIcon() instanceof ItemIcon) {
                this.getIcon().paint(matrices, x + 1, y + 1, iconSize);
            } else {
                this.getIcon().paint(matrices, x, y, iconSize);
            }
        }
    }

    @Override
    public void onClick(int x, int y, int button) {
        if (isEnabled() && isWithinBounds(x, y))
            if (getOnClick()!=null) getOnClick().run();
    }

    @Override
    public void renderTooltip(MatrixStack matrices, int x, int y, int tX, int tY) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen != null) screen.renderOrderedTooltip(matrices, Collections.singletonList(tooltip.asOrderedText()), tX+x, tY+y);
    }

    public void setIconSize(int iconSize) {
        this.iconSize = iconSize;
    }

    @Override
    public void setSize(int x, int y) {
        this.width = x;
        this.height = y;
    }
}
