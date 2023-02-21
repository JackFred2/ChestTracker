package red.jackf.chesttracker.util;

import io.github.cottonmc.cotton.gui.client.LibGui;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

import static red.jackf.chesttracker.ChestTracker.id;

// Holds 2 LibGui Icons, and returns them based on LibGui's dark mode
@Environment(EnvType.CLIENT)
public record DarkModeIcon(Icon lightModeIcon, Icon darkModeIcon) {
    public Icon get() {
        return LibGui.isDarkMode() ? darkModeIcon : lightModeIcon;
    }

    // assumes the dark_mode/light_mode folders
    public static DarkModeIcon fromFolder(String path) {
        return new DarkModeIcon(new TextureIcon(id("textures/light_mode/" + path)), new TextureIcon(id("textures/dark_mode/" + path)));
    }
}
