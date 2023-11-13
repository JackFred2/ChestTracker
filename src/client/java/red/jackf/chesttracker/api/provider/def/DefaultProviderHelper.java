package red.jackf.chesttracker.api.provider.def;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.api.gui.MemoryKeyIcon;
import red.jackf.chesttracker.provider.DefaultIcons;

import java.util.List;

/**
 * Methods for expanding upon the default Provider's icon set.
 */
public interface DefaultProviderHelper {

    /**
     * Register a given ItemStack as a key's icon. Registering to an already registered key will overwrite. This method
     * will add to the bottom of the list.
     *
     * @param key Key to register the icon for.
     * @param iconStack ItemStack to use as the icon.
     */
    static void registerIcon(ResourceLocation key, ItemStack iconStack) {
        DefaultIcons.INSTANCE.registerIcon(key, iconStack);
    }

    /**
     * Register a given ItemStack as a key's icon. Registering to an already registered key will overwrite. This method
     * will add the icon after the given icon, or place at the end of the list if not present.
     *
     * @param key Key to register the icon for.
     * @param iconStack ItemStack to use as the icon.
     * @param displayAfter Key to display the registered icon after.
     */
    static void registerIconAfter(ResourceLocation key, ItemStack iconStack, ResourceLocation displayAfter) {
        DefaultIcons.INSTANCE.registerIconAfter(key, iconStack, displayAfter);
    }

    /**
     * Register a given ItemStack as a key's icon. Registering to an already registered key will overwrite. This method
     * will add the icon before the given icon, or place at the start of the list if not present.
     *
     * @param key Key to register the icon for.
     * @param iconStack ItemStack to use as the icon.
     * @param displayBefore Key to display the registered icon before.
     */
    static void registerIconBefore(ResourceLocation key, ItemStack iconStack, ResourceLocation displayBefore) {
        DefaultIcons.INSTANCE.registerIconBefore(key, iconStack, displayBefore);
    }

    /**
     * Get a list of default key -> icon mappings.
     *
     * @return List of default key icons.
     */
    static List<MemoryKeyIcon> getDefaultIcons() {
        return DefaultIcons.INSTANCE.getIcons();
    }
}
