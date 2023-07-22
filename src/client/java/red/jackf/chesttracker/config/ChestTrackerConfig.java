package red.jackf.chesttracker.config;

import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.gui.MemoryKeyIcon;
import red.jackf.chesttracker.memory.LightweightStack;
import red.jackf.chesttracker.storage.Storage;
import red.jackf.chesttracker.util.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

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
                Files.move(path, path.resolveSibling(path.getFileName().toString() + ".errored"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                ChestTracker.LOGGER.fatal("Error backing up errored config", e);
            }
        }
        INSTANCE.save();
    }

    @ConfigEntry
    public Gui gui = new Gui();

    @ConfigEntry
    public Memory memory = new Memory();

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
        public int gridWidth = Constants.MIN_GRID_WIDTH;

        @ConfigEntry
        public int gridHeight = Constants.MIN_GRID_HEIGHT;

        @ConfigEntry
        public List<MemoryKeyIcon> memoryKeyIcons = new ArrayList<>(ICON_DEFAULTS);

        @ConfigEntry
        public boolean hideMemoryIds = false;

        private static final List<MemoryKeyIcon> ICON_DEFAULTS = List.of(
                new MemoryKeyIcon(ChestTracker.id("ender_chest"), new LightweightStack(Items.ENDER_CHEST)),
                new MemoryKeyIcon(Level.OVERWORLD.location(), new LightweightStack(Items.GRASS_BLOCK)),
                new MemoryKeyIcon(Level.NETHER.location(), new LightweightStack(Items.NETHERRACK)),
                new MemoryKeyIcon(Level.END.location(), new LightweightStack(Items.END_STONE))
        );
    }

    public static class Memory {
        @ConfigEntry
        public boolean autoLoadMemories = true;
        @ConfigEntry
        public boolean readableJsonMemories = false;

        @ConfigEntry
        public Storage.Backend storageBackend = Storage.Backend.JSON;
    }

    public void validate() {
        this.gui.gridWidth = Mth.clamp(this.gui.gridWidth, Constants.MIN_GRID_WIDTH, Constants.MAX_GRID_WIDTH);
        this.gui.gridHeight = Mth.clamp(this.gui.gridHeight, Constants.MIN_GRID_HEIGHT, Constants.MAX_GRID_HEIGHT);
        if (this.memory.storageBackend == null) this.memory.storageBackend = Storage.Backend.JSON;
    }
}
