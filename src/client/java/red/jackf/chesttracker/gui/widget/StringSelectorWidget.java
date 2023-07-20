package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.gui.util.CustomSearchablesFormatter;
import red.jackf.chesttracker.gui.util.NinePatcher;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class StringSelectorWidget extends AbstractWidget {
    private static final int ROW_HEIGHT = 12;
    private final Consumer<String> onSelect;
    private List<String> options = Collections.emptyList();

    public StringSelectorWidget(int x, int y, int width, int height, Component message, Consumer<String> onSelect) {
        super(x, y, width, height, message);
        this.onSelect = onSelect;
    }

    public void setOptions(List<String> options) {
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
        for (int i = 0; i < options.size() && i < this.getHeight() / ROW_HEIGHT; i++) {
            String option = options.get(i);
            var hovered = Objects.equals(getHoveredIndex(mouseX, mouseY), i);
            graphics.drawString(Minecraft.getInstance().font,
                    option,
                    this.getX() + 2 + (hovered ? 6 : 0),
                    this.getY() + 2 + ROW_HEIGHT * i,
                     hovered ? CustomSearchablesFormatter.getSearchTermColour() : CustomSearchablesFormatter.getTextColour());
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        var index = getHoveredIndex((int) mouseX, (int) mouseY);
        if (index != null && index >= 0 && index < options.size()) {
            onSelect.accept(options.get(index));
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
