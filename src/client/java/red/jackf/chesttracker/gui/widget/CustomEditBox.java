package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.util.GuiUtil;

public class CustomEditBox extends EditBox {
    public static final Component SEARCH_MESSAGE = Component.translatable("gui.recipebook.search_hint");

    public CustomEditBox(Font font, int x, int y, int width, int height, @Nullable EditBox editBox, Component message) {
        super(font, x, y, width, height, editBox, message);
        this.setBordered(false);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blitSprite(GuiUtil.SEARCH_BAR_SPRITE, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        graphics.pose().translate(2, 2, 0);
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        graphics.pose().translate(-2, -2, 0);
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
