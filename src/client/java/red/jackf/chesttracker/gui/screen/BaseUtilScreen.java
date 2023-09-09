package red.jackf.chesttracker.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.gui.GuiConstants;
import red.jackf.chesttracker.gui.util.NinePatcher;
import red.jackf.chesttracker.gui.util.TextColours;

public abstract class BaseUtilScreen extends Screen {
    protected int menuWidth;
    protected int menuHeight;
    protected int left;
    protected int top;

    protected BaseUtilScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

        this.menuWidth = GuiConstants.UTIL_GUI_WIDTH;
        this.menuHeight = GuiConstants.UTIL_GUI_HEIGHT;

        this.left = (this.width - menuWidth) / 2;
        this.top = (this.height - menuHeight) / 2;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        NinePatcher.BACKGROUND.draw(graphics, this.left, this.top, this.menuWidth, this.menuHeight);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(Minecraft.getInstance().font, this.title, left + GuiConstants.MARGIN, this.top + GuiConstants.MARGIN, TextColours.getLabelColour(), false);
    }
}
