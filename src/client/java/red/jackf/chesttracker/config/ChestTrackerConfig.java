package red.jackf.chesttracker.config;

import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;

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
        public int gridWidth = 9;

        @ConfigEntry
        public int gridHeight = 6;
    }

    public void validate() {
        this.gui.gridWidth = Mth.clamp(this.gui.gridWidth, 9, 18);
        this.gui.gridHeight = Mth.clamp(this.gui.gridHeight, 6, 12);
    }
}
