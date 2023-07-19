package red.jackf.chesttracker.gui.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws the background for the GUI, with dynamic sizes.
 */
public record NinePatcher(ResourceLocation texture, int uvStartX, int uvStartY, int patchSize, int patchGap) {

    public void draw(GuiGraphics graphics, int x, int y, int width, int height) {
        if (height < 2 * patchSize) throw new IllegalArgumentException("Height %d too small; must be at least %d".formatted(height, patchSize * 2));
        if (width < 2 * patchSize) throw new IllegalArgumentException("Width %d too small; must be at least %d".formatted(width, patchSize * 2));

        int midOffset =  patchSize + patchGap;
        int farOffset =  2 * (patchSize + patchGap);
        int twiceSize =  2 * patchSize;

        boolean noMidColumn = width == twiceSize;
        boolean noMidRow = height == twiceSize;

        // top left
        blit(graphics, x, y, patchSize, patchSize, uvStartX, uvStartY);
        // top right
        blit(graphics, x + width - patchSize, y, patchSize, patchSize, uvStartX + farOffset, uvStartY);
        // bottom left
        blit(graphics, x, y + height - patchSize, patchSize, patchSize, uvStartX, uvStartY + farOffset);
        // bottom right
        blit(graphics, x + width - patchSize, y + height - patchSize, patchSize, patchSize, uvStartX + farOffset, uvStartY + farOffset);

        if (!noMidRow) {
            // top
            blit(graphics, x + patchSize, y, width - twiceSize, patchSize, uvStartX + midOffset, uvStartY);
            // bottom
            blit(graphics, x + patchSize, y + height - patchSize, width - twiceSize, patchSize, uvStartX + midOffset, uvStartY + farOffset);
        }

        if (!noMidColumn) {
            // left
            blit(graphics, x, y + patchSize, patchSize, height - twiceSize, uvStartX, uvStartY + midOffset);
            // right
            blit(graphics, x + width - patchSize, y + patchSize, patchSize, height - twiceSize, uvStartX + farOffset, uvStartY + midOffset);
        }

        // center
        if (!noMidRow && !noMidColumn)
            blit(graphics, x + patchSize, y + patchSize, width - twiceSize, height - twiceSize, uvStartX + midOffset, uvStartY + midOffset);
    }

    private void blit(GuiGraphics graphics, int x, int y, int width, int height, int uvX, int uvY) {
        graphics.blit(texture, x, y, width, height, uvX, uvY, patchSize, patchSize, 256, 256);
    }
}
