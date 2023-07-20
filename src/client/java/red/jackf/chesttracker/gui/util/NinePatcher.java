package red.jackf.chesttracker.gui.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import red.jackf.chesttracker.ChestTracker;

/**
 * Shortcutter for GuiGraphcs#blitNineSliced. Expects a square texture.
 */
public final class NinePatcher {
    public static final NinePatcher BACKGROUND = new NinePatcher(ChestTracker.guiTex("9patch/background"), 6, 18, InnerMode.TILE);
    public static final NinePatcher SEARCH = new NinePatcher(ChestTracker.guiTex("9patch/search_bar"), 2, 8, InnerMode.STRETCH);

    private final ResourceLocation texture;
    private final int outerSize;
    private final int innerSize;
    private final InnerMode innerMode;
    private final int diameter;
    public NinePatcher(ResourceLocation texture, int outerSize, int innerSize, InnerMode innerMode) {
        this.texture = texture;
        this.outerSize = outerSize;
        this.innerSize = innerSize;
        this.innerMode = innerMode;
        this.diameter = 2 * outerSize + innerSize;
    }

    /**
     * Returns an integer that will tile nicely if applicable, decreasing the size if needed - used for various menus
     */
    public int fitsNicely(int size) {
        return innerMode == InnerMode.TILE ? size - ((size - 2 * outerSize) % innerSize) : size;
    }

    public void draw(GuiGraphics graphics, int x1, int y1, int width, int height) {
        if (width < outerSize * 2) return;
        if (height < outerSize * 2) return;
        int x2 = x1 + outerSize;
        int x4 = x1 + width;
        int x3 = x4 - outerSize;
        int y2 = y1 + outerSize;
        int y4 = y1 + height;
        int y3 = y4 - outerSize;

        // top left
        blitStatic(graphics, x1, y1, outerSize, outerSize, 0, 0);
        // top right
        blitStatic(graphics, x3, y1, outerSize, outerSize, diameter - outerSize, 0);
        // bottom left
        blitStatic(graphics, x1, y3, outerSize, outerSize, 0, diameter - outerSize);
        // bottom right
        blitStatic(graphics, x3, y3, outerSize, outerSize, diameter - outerSize, diameter - outerSize);

        if (x2 != x3) {
            // top
            blit(graphics, x2, y1, x3, y2, outerSize, 0, innerSize, outerSize);
            // bottom
            blit(graphics, x2, y3, x3, y4, outerSize, diameter - outerSize, innerSize, outerSize);
        }

        // middle row
        if (y2 != y3) {
            // left
            blit(graphics, x1, y2, x2, y3, 0, outerSize, outerSize, innerSize);
            // right
            blit(graphics, x3, y2, x4, y3, diameter - outerSize, outerSize, outerSize, innerSize);
        }

        // center
        if (x2 != x3 && y2 != y3) {
            blit(graphics, x2, y2, x3, y3, outerSize, outerSize, innerSize, innerSize);
        }
    }

    private void blit(GuiGraphics graphics, int x1, int y1, int x2, int y2, int uvX, int uvY, int uvWidth, int uvHeight) {
        if (innerMode == InnerMode.STRETCH) {
            graphics.blit(texture, x1, y1, x2 - x1, y2 - y1, uvX, uvY, uvWidth, uvHeight, diameter, diameter);
        } else {
            int rows = Mth.positiveCeilDiv(y2 - y1, innerSize);
            int columns = Mth.positiveCeilDiv(x2 - x1, innerSize);
            for (int row = 0; row < rows; row++) {
                int y = y1 + row * innerSize;
                for (int column = 0; column < columns; column++) {
                    int x = x1 + column * innerSize;
                    int width = Math.min(innerSize, x2 - x);
                    int height = Math.min(innerSize, y2 - y);
                    graphics.blit(texture, x, y, width, height, uvX, uvY, width, height, diameter, diameter);
                }
            }
        }
    }

    private void blitStatic(GuiGraphics graphics, int x, int y, int width, int height, int uvX, int uvY) {
        graphics.blit(texture, x, y, width, height, uvX, uvY, width, height, diameter, diameter);
    }

    public enum InnerMode {
        STRETCH,
        TILE
    }
}
