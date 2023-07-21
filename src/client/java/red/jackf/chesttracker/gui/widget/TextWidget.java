package red.jackf.chesttracker.gui.widget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public final class TextWidget implements Renderable {
    private final int x;
    private final int y;
    private final Component text;
    private final Component obfuscated;
    private final int colour;
    private final boolean obfuscatedUntilHover;

    public TextWidget(int x, int y, Component text, int colour, boolean obfuscatedUntilHover) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.colour = colour;
        this.obfuscatedUntilHover = obfuscatedUntilHover;
        if (obfuscatedUntilHover)
            this.obfuscated = text.copy().withStyle(ChatFormatting.OBFUSCATED);
        else
            this.obfuscated = null;
    }


    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        if (!obfuscatedUntilHover) {
            graphics.drawString(font, text, x, y, colour, false);
        } else {
            var width = font.width(text);
            var height = font.lineHeight;
            var hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
            if (hovered) {
                graphics.drawString(font, text, x, y, colour, false);
            } else {
                graphics.drawString(font, obfuscated, x, y, colour, false);
            }
        }
    }
}
