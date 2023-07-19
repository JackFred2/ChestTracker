package red.jackf.chesttracker.config;

import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.gui.MemoryIcon;
import red.jackf.chesttracker.memory.LightweightStack;
import red.jackf.chesttracker.storage.Storage;
import red.jackf.chesttracker.util.Constants;

import java.io.IOException;
import java.nio.file.Files;
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
                Files.move(path, path.resolveSibling(path.getFileName().toString() + ".errored"));
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
        public List<MemoryIcon> memoryIcons = new ArrayList<>(ICON_DEFAULTS);

        private static final List<MemoryIcon> ICON_DEFAULTS = List.of(
                new MemoryIcon(ChestTracker.id("ender_chest"), new LightweightStack(Items.ENDER_CHEST)),
                new MemoryIcon(Level.OVERWORLD.location(), new LightweightStack(Items.GRASS_BLOCK)),
                new MemoryIcon(Level.NETHER.location(), new LightweightStack(Items.NETHERRACK)),
                new MemoryIcon(Level.END.location(), new LightweightStack(Items.END_STONE))
        );
    }

    public static class Memory {
        @ConfigEntry
        public boolean readableMemories = false;

        @ConfigEntry
        public Storage.Backend storageBackend = Storage.Backend.JSON;
    }

    public void validate() {
        this.gui.gridWidth = Mth.clamp(this.gui.gridWidth, Constants.MIN_GRID_WIDTH, Constants.MAX_GRID_WIDTH);
        this.gui.gridHeight = Mth.clamp(this.gui.gridHeight, Constants.MIN_GRID_HEIGHT, Constants.MAX_GRID_HEIGHT);
        if (this.memory.storageBackend == null) this.memory.storageBackend = Storage.Backend.JSON;
    }
}
