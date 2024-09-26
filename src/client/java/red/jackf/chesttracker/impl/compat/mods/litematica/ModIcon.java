package red.jackf.chesttracker.impl.compat.mods.litematica;

import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.impl.ChestTracker;

public enum ModIcon implements IGuiIcon {
    INSTANCE;

    @Override
    public int getWidth() {
        return 11;
    }

    @Override
    public int getHeight() {
        return 11;
    }

    @Override
    public int getU() {
        return 0;
    }

    @Override
    public int getV() {
        return 0;
    }

    @Override
    public void renderAt(int x, int y, float z, boolean enabled, boolean selected) {
        RenderUtils.drawTexturedRect(x, y, getU(), getV(), getWidth(), getHeight(), z);
    }

    @Override
    public ResourceLocation getTexture() {
        return ChestTracker.id("textures/gui/litematica_icon.png");
    }
}
