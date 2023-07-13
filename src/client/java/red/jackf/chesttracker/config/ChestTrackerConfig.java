package red.jackf.chesttracker.config;

import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.memory.LightweightStack;
import red.jackf.chesttracker.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class ChestTrackerConfig {
    public static final GsonConfigInstance<ChestTrackerConfig> INSTANCE
            = GsonConfigInstance.createBuilder(ChestTrackerConfig.class)
            .setPath(FabricLoader.getInstance().getConfigDir().resolve("chesttracker.json"))
            .overrideGsonBuilder(ChestTrackerGSON.get())
            .build();

    @ConfigEntry
    public Gui gui = new Gui();

    public static class Gui {

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
                new MemoryIcon(Level.OVERWORLD.location(), new LightweightStack(Items.GRASS_BLOCK)),
                new MemoryIcon(Level.NETHER.location(), new LightweightStack(Items.NETHERRACK)),
                new MemoryIcon(Level.END.location(), new LightweightStack(Items.END_STONE)),

                new MemoryIcon(ChestTracker.id("ender_chest"), new LightweightStack(Items.ENDER_CHEST))
        );
    }

    public void validate() {
        this.gui.gridWidth = Mth.clamp(this.gui.gridWidth, Constants.MIN_GRID_WIDTH, Constants.MAX_GRID_WIDTH);
        this.gui.gridHeight = Mth.clamp(this.gui.gridHeight, Constants.MIN_GRID_HEIGHT, Constants.MAX_GRID_HEIGHT);
    }
}
