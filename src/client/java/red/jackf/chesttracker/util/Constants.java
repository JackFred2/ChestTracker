package red.jackf.chesttracker.util;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class Constants {

    public static final Path STORAGE_DIR = FabricLoader.getInstance().getGameDir().resolve("chesttracker");
}
