package red.jackf.chesttracker.util;

import io.github.cottonmc.cotton.gui.client.LibGui;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

import static red.jackf.chesttracker.ChestTracker.id;

// Holds 2 texture paths, and returns them based on LibGui's dark mode
@Environment(EnvType.CLIENT)
public record DarkModeTexture(Identifier lightModeTexture, Identifier darkModeTexture) {
    public Identifier get() {
        return LibGui.isDarkMode() ? darkModeTexture : lightModeTexture;
    }

    // assumes the dark_mode/light_mode folders
    public static DarkModeTexture fromFolder(String path) {
        return new DarkModeTexture(id("textures/light_mode/" + path), id("textures/dark_mode/" + path));
    }
}
