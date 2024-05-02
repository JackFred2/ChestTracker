package red.jackf.chesttracker.impl.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.impl.gui.util.SpriteSet;

public class SpriteButton extends Button {
    private SpriteSet sprites;

    public SpriteButton(int x, int y, int width, int height, SpriteSet sprites, Button.OnPress callback) {
        super(x, y, width, height, CommonComponents.EMPTY, callback, DEFAULT_NARRATION);
        this.sprites = sprites;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation tex = this.sprites.get(this.isActive(), this.isHoveredOrFocused());
        this.renderTexture(
                graphics,
                tex,
                this.getX(),
                this.getY(),
                0,
                0,
                0,
                this.width,
                this.height,
                this.width,
                this.height
        );
    }
}
