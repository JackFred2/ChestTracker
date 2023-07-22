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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Displays a list of components (the values in the `options` map), and runs a callback on selection of one. Recommended to use
 * a {@link LinkedHashMap} if you want a specific ordering.
 *
 * @param <T> Type of result to be returned
 */
public class StringSelectorWidget<T> extends AbstractWidget {
    private static final int ROW_HEIGHT = 12;
    private final Consumer<T> onSelect;
    private Map<T, Component> options = Collections.emptyMap();
    @Nullable
    private T lastHovered = null;
    @Nullable
    private T highlight = null;

    public StringSelectorWidget(int x, int y, int width, int height, Component message, Consumer<T> onSelect) {
        super(x, y, width, height, message);
        this.onSelect = onSelect;
    }

    public void setHighlight(@Nullable T highlight) {
        this.highlight = highlight;
    }

    public void setOptions(Map<T, Component> options) {
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
        lastHovered = null;
        for (var entry : options.entrySet()) {
            if (i >= this.getHeight() / ROW_HEIGHT) break;
            boolean hovered = Objects.equals(i, hoveredIndex);
            if (hovered) lastHovered = entry.getKey();
            var textColour = hovered ? TextColours.getSearchTermColour() : entry.getKey().equals(highlight) ? TextColours.getSearchKeyColour() : TextColours.getSearchTextColour();
            graphics.drawString(Minecraft.getInstance().font,
                    entry.getValue(),
                    this.getX() + 2 + (hovered ? 6 : 0),
                    this.getY() + 2 + ROW_HEIGHT * i,
                    textColour);
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
