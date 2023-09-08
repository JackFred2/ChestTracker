package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public final class TextWidget implements Renderable {
    private final int x;
    private final int y;
    private final int width;
    private final Component text;
    private final int colour;
    private final Alignment alignment;

    public TextWidget(int x, int y, int width, Component text, int colour, Alignment alignment) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.text = text;
        this.colour = colour;
        this.alignment = alignment;
    }

    public TextWidget(int x, int y, Component text, int colour) {
        this(x, y, Minecraft.getInstance().font.width(text), text, colour, Alignment.LEFT);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        var textX = x + (int) (alignment.factor * (width - font.width(text)));
        graphics.drawString(font, text, textX, y, colour, false);
    }

    public enum Alignment {
        LEFT(0.0f),
        CENTER(0.5f),
        RIGHT(1.0f);

        final float factor;

        Alignment(float factor) {
            this.factor = factor;
        }
    }
}
