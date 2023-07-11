package red.jackf.chesttracker.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws the background for the GUI, with dynamic sizes.
 */
public record NinePatcher(ResourceLocation texture, int uvStartX, int uvStartY, int patchSize, int patchGap) {

    void draw(GuiGraphics graphics, int x, int y, int width, int height) {
        int midOffset =  patchSize + patchGap;
        int farOffset =  2 * (patchSize + patchGap);
        int twiceSize =  2 * patchSize;

        // top left
        blit(graphics, x, y, patchSize, patchSize, uvStartX, uvStartY);
        // top right
        blit(graphics, x + width - patchSize, y, patchSize, patchSize, uvStartX + farOffset, uvStartY);
        // bottom left
        blit(graphics, x, y + height - patchSize, patchSize, patchSize, uvStartX, uvStartY + farOffset);
        // bottom right
        blit(graphics, x + width - patchSize, y + height - patchSize, patchSize, patchSize, uvStartX + farOffset, uvStartY + farOffset);

        // top
        blit(graphics, x + patchSize, y, width - twiceSize, patchSize, uvStartX + midOffset, uvStartY);
        // bottom
        blit(graphics, x + patchSize, y + height - patchSize, width - twiceSize, patchSize, uvStartX + midOffset, uvStartY + farOffset);
        // left
        blit(graphics, x, y + patchSize, patchSize, height - twiceSize, uvStartX, uvStartY + midOffset);
        // right
        blit(graphics, x + width - patchSize, y + patchSize, patchSize, height - twiceSize, uvStartX + farOffset, uvStartY + midOffset);

        // center
        blit(graphics, x + patchSize, y + patchSize, width - twiceSize, height - twiceSize, uvStartX + midOffset, uvStartY + midOffset);
    }

    private void blit(GuiGraphics graphics, int x, int y, int width, int height, int uvX, int uvY) {
        graphics.blit(texture, x, y, width, height, uvX, uvY, patchSize, patchSize, 256, 256);
    }
}
