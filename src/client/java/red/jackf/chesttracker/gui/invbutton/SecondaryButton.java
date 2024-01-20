package red.jackf.chesttracker.gui.invbutton;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static red.jackf.chesttracker.gui.invbutton.InventoryButton.Z_OFFSET;

public class SecondaryButton extends AbstractWidget {
    private final WidgetSprites sprites;

    public SecondaryButton(int x, int y, WidgetSprites sprites, Component message) {
        super(x, y, InventoryButton.SIZE, InventoryButton.SIZE, message);
        this.sprites = sprites;
        this.visible = false;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture = sprites.get(this.isActive(), this.isHoveredOrFocused());
        graphics.blitSprite(texture, this.getX(), this.getY(), Z_OFFSET, this.width, this.height);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        System.out.println("clicked");
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
