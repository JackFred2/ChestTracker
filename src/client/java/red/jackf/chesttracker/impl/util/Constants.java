package red.jackf.chesttracker.impl.util;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class Constants {

    // TODO: Make user configurable
    public static final Path STORAGE_DIR = FabricLoader.getInstance().getGameDir().resolve("chesttracker");
}
