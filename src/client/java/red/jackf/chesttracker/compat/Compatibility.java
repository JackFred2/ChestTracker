package red.jackf.chesttracker.compat;

import net.fabricmc.loader.api.FabricLoader;

public interface Compatibility {
    boolean SEARCHABLES = FabricLoader.getInstance().isModLoaded("searchables");
}
