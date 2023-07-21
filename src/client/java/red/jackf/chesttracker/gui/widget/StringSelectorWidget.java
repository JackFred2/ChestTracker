package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.gui.util.NinePatcher;
import red.jackf.chesttracker.gui.util.TextColours;

import java.util.*;
import java.util.function.Consumer;

/**
 * @param <T> Result to be returned
 */
public class StringSelectorWidget<T> extends AbstractWidget {
    private static final int ROW_HEIGHT = 12;
    private final Consumer<T> onSelect;
    private Map<T, String> options = Collections.emptyMap();
    @Nullable
    private T lastHovered = null;

    public StringSelectorWidget(int x, int y, int width, int height, Component message, Consumer<T> onSelect) {
        super(x, y, width, height, message);
        this.onSelect = onSelect;
    }

    public void setOptions(Map<T, String> options) {
        this.options = options;
    }

    @Nullable
    private Integer getHoveredIndex(int mouseX, int mouseY) {
        if (!this.isMouseOver(mouseX, mouseY)) return null;
        return Math.floorDiv((mouseY - 1 - getY()), ROW_HEIGHT);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        NinePatcher.SEARCH.draw(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        int i = 0;
        var hoveredIndex = getHoveredIndex(mouseX, mouseY);
        for (var entry : options.entrySet()) {
            if (i >= this.getHeight() / ROW_HEIGHT) break;
            boolean hovered = Objects.equals(i, hoveredIndex);
            if (hovered) lastHovered = entry.getKey();
            graphics.drawString(Minecraft.getInstance().font,
                    entry.getValue(),
                    this.getX() + 2 + (hovered ? 6 : 0),
                    this.getY() + 2 + ROW_HEIGHT * i,
                    hovered ? TextColours.getSearchTermColour() : TextColours.getSearchTextColour());
            i++;
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (lastHovered != null) onSelect.accept(lastHovered);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
