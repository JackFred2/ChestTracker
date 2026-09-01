package red.jackf.chesttracker.impl.compat.mods.litematica;

import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.impl.ChestTracker;

public enum ModIcon implements IGuiIcon {
    INSTANCE;

    private final int u = 0;
    private final int v = 0;
    private final int w = 11;
    private final int h = 11;

    @Override
    public int getWidth() {
        return this.w;
    }

    @Override
    public int getHeight() {
        return this.h;
    }

    @Override
    public int getU() {
        return this.u;
    }

    @Override
    public int getV() {
        return this.v;
    }

    @Override
    public void renderAt(GuiGraphics graphics, int x, int y, float zLevel, boolean enabled, boolean selected) {
        RenderUtils.drawTexturedRect(graphics, this.getTexture(), x, y, this.u, this.v, this.w, this.h, zLevel);
    }

    @Override
    public ResourceLocation getTexture() {
        return ChestTracker.id("textures/gui/litematica_icon.png");
    }
}
