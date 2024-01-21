package red.jackf.chesttracker.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.gui.GuiConstants;
import red.jackf.chesttracker.storage.backend.Backend.Type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ChestTrackerConfig {
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("chesttracker.json5");
    public static final ConfigClassHandler<ChestTrackerConfig> INSTANCE
            = ConfigClassHandler.createBuilder(ChestTrackerConfig.class)
            .id(ChestTracker.id("config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(PATH)
                    .setJson5(true)
                    .build())
            .build();

    public static void init() {
        try {
            INSTANCE.load();
            INSTANCE.instance().validate();
        } catch (Exception ex) {
            ChestTracker.LOGGER.error("Error loading Chest Tracker config, backing it up and restoring default", ex);
            try {
                Files.move(PATH, PATH.resolveSibling(PATH.getFileName().toString() + ".errored"),
                           StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                ChestTracker.LOGGER.fatal("Error backing up errored config", e);
            }
        }
        INSTANCE.save();
    }
    @SerialEntry
    public Gui gui = new Gui();
    @SerialEntry
    public Rendering rendering = new Rendering();
    @SerialEntry
    public Debug debug = new Debug();
    @SerialEntry
    public Storage storage = new Storage();
    @SerialEntry
    public Compatibility compatibility = new Compatibility();

    public static class Gui {
        @SerialEntry(comment = "Whether to automatically focus the search bar when the GUI is opened.")
        public boolean autofocusSearchBar = false;

        @SerialEntry(comment = "Show Autocomplete for Search Bar.")
        public boolean showAutocomplete = true;

        @SerialEntry(comment = "Show Unnamed Items in Autocomplete.")
        public boolean autocompleteShowsRegularNames = true;

        @SerialEntry(comment = "Show Resize Widget.")
        public boolean showResizeWidget = true;

        @SerialEntry(comment = "Grid Width. Range: [9, 18] slots")
        public int gridWidth = GuiConstants.MIN_GRID_COLUMNS;

        @SerialEntry(comment = "Grid Height. Range: [6, 12] slots")
        public int gridHeight = GuiConstants.MIN_GRID_ROWS;

        @SerialEntry(comment = "Hide the Memory Bank ID from the edit GUIs, for example in case you want to hide an IP.")
        public boolean hideMemoryIds = false;

        @SerialEntry(comment = "How to scale the text in-GUI, relative to Minecraft's GUI scale? Minimum of 1.")
        public int itemListTextScale = 0;

        @SerialEntry
        public InventoryButton inventoryButton = new InventoryButton();
        public static class InventoryButton {
            @SerialEntry(comment = "Whether to enable the button that appears in inventory screens.")
            public boolean enabled = true;
            @SerialEntry(comment = "If relevant, show additional buttons after hovering over the main button, such as deletion and filtering.")
            public boolean showExtra = true;
        }
    }

    public static class Rendering {
        @SerialEntry(comment = "Name Render Range. Range: [4, 24] blocks")
        public int nameRange = 12;
    }

    public static class Debug {
        @SerialEntry(comment = "DEBUG: Show Developer Hud")
        public boolean showDevHud = false;
    }

    public static class Storage {
        @SerialEntry(comment = "Whether the JSON files in the memory directory should be readable, or compacted.")
        public boolean readableJsonMemories = false;

        @SerialEntry(comment = "Defines the format that Memory Banks are stored between worlds/sessions. Valid values: NBT, JSON, MEMORY")
        public Type storageBackend = Type.NBT;
    }

    public static class Compatibility {
        @SerialEntry
        public boolean shulkerBoxTooltipIntegration = true;
        @SerialEntry
        public boolean wthitIntegration = true;
    }

    public void validate() {
        this.gui.gridWidth = Mth.clamp(this.gui.gridWidth, GuiConstants.MIN_GRID_COLUMNS, GuiConstants.MAX_GRID_WIDTH);
        this.gui.gridHeight = Mth.clamp(this.gui.gridHeight, GuiConstants.MIN_GRID_ROWS, GuiConstants.MAX_GRID_HEIGHT);
        this.gui.itemListTextScale = Mth.clamp(this.gui.itemListTextScale, -6, 0);
        this.rendering.nameRange = Mth.clamp(this.rendering.nameRange, 4, 24);
        if (this.storage.storageBackend == null) this.storage.storageBackend = Type.NBT;
    }
}
