package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public final class TextWidget implements Renderable {
    private final int x;
    private final int y;
    private final Component text;
    private final int colour;

    public TextWidget(int x, int y, Component text, int colour) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.colour = colour;
    }


    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, text, x, y, colour, false);
    }
}
