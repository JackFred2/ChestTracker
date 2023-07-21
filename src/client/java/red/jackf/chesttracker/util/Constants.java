package red.jackf.chesttracker.util;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class Constants {
    public static final int SLOT_SIZE = 18;
    public static final int MIN_GRID_WIDTH = 9;
    public static final int MAX_GRID_WIDTH = 18;
    public static final int MIN_GRID_HEIGHT = 6;
    public static final int MAX_GRID_HEIGHT = 12;

    public static final Path STORAGE_DIR = FabricLoader.getInstance().getGameDir().resolve("chesttracker");
}