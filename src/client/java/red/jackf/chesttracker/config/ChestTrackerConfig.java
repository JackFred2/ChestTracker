package red.jackf.chesttracker.config;

import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;
import red.jackf.chesttracker.util.Constants;

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
    }

    public void validate() {
        this.gui.gridWidth = Mth.clamp(this.gui.gridWidth, Constants.MIN_GRID_WIDTH, Constants.MAX_GRID_WIDTH);
        this.gui.gridHeight = Mth.clamp(this.gui.gridHeight, Constants.MIN_GRID_HEIGHT, Constants.MAX_GRID_HEIGHT);
    }
}
