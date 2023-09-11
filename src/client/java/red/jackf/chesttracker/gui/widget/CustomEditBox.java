package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.gui.util.NinePatcher;
import red.jackf.chesttracker.mixins.EditBoxAccessor;

public class CustomEditBox extends EditBox {
    public CustomEditBox(Font font, int x, int y, int width, int height, @Nullable EditBox editBox, Component component) {
        super(font, x, y, width, height, editBox, component);
        this.setBordered(false);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        NinePatcher.SEARCH.draw(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        final int yInset = Screen.hasAltDown() ? 2 : (1 + this.getHeight() - ((EditBoxAccessor) this).getFont().lineHeight) / 2;
        final int xInset = 1 + yInset / 2;
        guiGraphics.pose().translate(xInset, yInset, 0);
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().translate(-xInset, -yInset, 0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_2) {
            this.setValue("");
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
