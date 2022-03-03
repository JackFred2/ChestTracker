package red.jackf.chesttracker.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import red.jackf.chesttracker.ChestTracker;

@Environment(EnvType.CLIENT)
@Config(name = ChestTracker.MODID)
@Config.Gui.CategoryBackground(category = "visual_options", background = "minecraft:textures/block/fire_coral_block.png")
@Config.Gui.CategoryBackground(category = "database_options", background = "minecraft:textures/block/bubble_coral_block.png")
@Config.Gui.CategoryBackground(category = "misc_options", background = "minecraft:textures/block/tube_coral_block.png")
public class ChestTrackerConfig implements ConfigData {

    @ConfigEntry.Category("visual_options")
    @ConfigEntry.Gui.TransitiveObject
    public VisualOptions visualOptions = new VisualOptions();

    @ConfigEntry.Category("database_options")
    @ConfigEntry.Gui.TransitiveObject
    public DatabaseOptions databaseOptions = new DatabaseOptions();

    @ConfigEntry.Category("misc_options")
    @ConfigEntry.Gui.TransitiveObject
    public MiscOptions miscOptions = new MiscOptions();

    @Override
    public void validatePostLoad() {
        visualOptions.borderColour = MathHelper.clamp(visualOptions.borderColour, 0, 0xffffff);
        visualOptions.nameRenderRange = MathHelper.clamp(visualOptions.nameRenderRange, 0, 16);
        visualOptions.rowCount = MathHelper.clamp(visualOptions.rowCount, 6, 12);
        visualOptions.columnCount = MathHelper.clamp(visualOptions.columnCount, 9, 18);
        databaseOptions.destroyedMemoryCheckInterval = MathHelper.clamp(databaseOptions.destroyedMemoryCheckInterval, 0, 60);
        miscOptions.searchRange = MathHelper.clamp(miscOptions.searchRange, 1, 98);
    }

    public static class VisualOptions {
        @ConfigEntry.ColorPicker
        public int borderColour = 0x00baff;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 16)
        public int nameRenderRange = 12;
        @ConfigEntry.BoundedDiscrete(min = 6, max = 12)
        public int rowCount = 6;
        @ConfigEntry.BoundedDiscrete(min = 9, max = 18)
        public int columnCount = 9;
        public boolean hideDeleteButton = false;
        public boolean enableButton = true;
        public boolean hideDatabaseInfo = false;
    }

    public static class DatabaseOptions {
        @ConfigEntry.BoundedDiscrete(min = 1, max = 60)
        @ConfigEntry.Gui.PrefixText
        public int destroyedMemoryCheckInterval = 10;
        @ConfigEntry.Gui.RequiresRestart
        @ConfigEntry.Gui.PrefixText
        public boolean readableFiles = false;
        @ConfigEntry.Gui.PrefixText
        public boolean safeFileNames = false;
        public boolean prefixFilesWithUsername = false;
    }

    public static class MiscOptions {
        public boolean printGuiClassNames = false;
        @ConfigEntry.Gui.Excluded
        public boolean rememberNewChests = true;
        @ConfigEntry.Gui.Excluded
        public int searchRange = 81; // 512 blocks
    }
}
