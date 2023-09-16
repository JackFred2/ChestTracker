package red.jackf.chesttracker.config;

import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.gui.GuiConstants;
import red.jackf.chesttracker.storage.backend.Backend.Type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ChestTrackerConfig {
    public static final GsonConfigInstance<ChestTrackerConfig> INSTANCE
            = GsonConfigInstance.createBuilder(ChestTrackerConfig.class)
            .setPath(FabricLoader.getInstance().getConfigDir().resolve("chesttracker.json"))
            .overrideGsonBuilder(ChestTrackerGSON.get())
            .build();

    public static void init() {
        try {
            INSTANCE.load();
            INSTANCE.getConfig().validate();
        } catch (Exception ex) {
            ChestTracker.LOGGER.error("Error loading Chest Tracker config, backing it up and restoring default", ex);
            var path = INSTANCE.getPath();
            try {
                Files.move(path, path.resolveSibling(path.getFileName()
                        .toString() + ".errored"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                ChestTracker.LOGGER.fatal("Error backing up errored config", e);
            }
        }
        INSTANCE.save();
    }

    @ConfigEntry
    public Gui gui = new Gui();

    @ConfigEntry
    public Rendering rendering = new Rendering();

    @ConfigEntry
    public Storage storage = new Storage();

    public static class Gui {

        @ConfigEntry
        public boolean autofocusSearchBar = true;

        @ConfigEntry
        public boolean showAutocomplete = true;

        @ConfigEntry
        public boolean autocompleteShowsRegularNames = true;

        @ConfigEntry
        public boolean showResizeWidget = true;

        @ConfigEntry
        public int gridWidth = GuiConstants.MIN_GRID_COLUMNS;

        @ConfigEntry
        public int gridHeight = GuiConstants.MIN_GRID_ROWS;

        @ConfigEntry
        public boolean hideMemoryIds = false;

        @ConfigEntry
        public boolean showDevHud = false;
    }

    public static class Rendering {
        @ConfigEntry
        public int nameRange = 12;
    }

    public static class Storage {
        @ConfigEntry
        public boolean readableJsonMemories = false;

        @ConfigEntry
        public Type storageBackend = Type.NBT;
    }

    public void validate() {
        this.gui.gridWidth = Mth.clamp(this.gui.gridWidth, GuiConstants.MIN_GRID_COLUMNS, GuiConstants.MAX_GRID_WIDTH);
        this.gui.gridHeight = Mth.clamp(this.gui.gridHeight, GuiConstants.MIN_GRID_ROWS, GuiConstants.MAX_GRID_HEIGHT);
        this.rendering.nameRange = Mth.clamp(this.rendering.nameRange, 4, 24);
        if (this.storage.storageBackend == null) this.storage.storageBackend = Type.NBT;
    }
}
