package red.jackf.chesttracker.api.providers.defaults;

import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.api.providers.MemoryKeyIcon;
import red.jackf.chesttracker.impl.providers.DefaultIconsImpl;

import java.util.List;

/**
 * Interface for getting and appending to the default memory key icons.
 */
public interface DefaultIcons {
    /**
     * Returns the list of icons used by the default provider. By default, this includes the Ender Chest, the 3 vanilla
     * dimensions and some mod compatibility.
     *
     * @return The list of memory key icons used by the default provider.
     */
    static List<MemoryKeyIcon> getDefaultIcons() {
        return DefaultIconsImpl.getDefaultIcons();
    }

    /**
     * Register a new icon for a memory key ID. This method adds it to the end of the list arbitrarily.
     *
     * @param icon Icon to add to the default list.
     */
    static void registerIcon(MemoryKeyIcon icon) {
        DefaultIconsImpl.registerIcon(icon);
    }

    /**
     * Register a new icon for a memory key ID. This method adds it above another icon if present, or to the top of the list
     * otherwise.
     *
     * @param target Target ID to place the icon above.
     * @param icon Icon to add to the default list.
     */
    static void registerIconAbove(ResourceLocation target, MemoryKeyIcon icon) {
        DefaultIconsImpl.registerIconAbove(target, icon);
    }

    /**
     * Register a new icon for a memory key ID. This method adds it below another icon if present, or to the bottom of the list
     * otherwise.
     *
     * @param target Target ID to place the icon below.
     * @param icon Icon to add to the default list.
     */
    static void registerIconBelow(ResourceLocation target, MemoryKeyIcon icon) {
        DefaultIconsImpl.registerIconBelow(target, icon);
    }
}
