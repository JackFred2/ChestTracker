package red.jackf.chesttracker.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import red.jackf.chesttracker.ChestTracker;

import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
@Config(name = ChestTracker.MODID)
@Config.Gui.CategoryBackground(category = "general", background = "minecraft:textures/block/fire_coral_block.png")
@Config.Gui.CategoryBackground(category = "tracked_guis", background = "minecraft:textures/block/brain_coral_block.png")
public class ChestTrackerConfig implements ConfigData {

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.TransitiveObject
    public GeneralOptions generalOptions = new GeneralOptions();

    @ConfigEntry.Category("tracked_guis")
    @ConfigEntry.Gui.TransitiveObject
    public TrackedScreens trackedScreens = new TrackedScreens();

    public static class TrackedScreens {
        @ConfigEntry.Gui.PrefixText
        public Boolean debugPrint = false;
        public List<String> blocklist = Arrays.asList(
                "AnvilScreen",
                "BeaconScreen",
                "CartographyTableScreen",
                "CraftingScreen",
                "CreativeInventoryScreen",
                "EnchantmentScreen",
                "GrindstoneScreen",
                "HorseScreen",
                "InventoryScreen",
                "LoomScreen",
                "MerchantScreen",
                "SmithingScreen",
                "StonecutterScreen"
        );
    }

    public static class GeneralOptions {
        @ConfigEntry.BoundedDiscrete(min = 0, max = 64)
        public int displayRange = 32;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 300)
        public int fadeOutTime = 140;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 200)
        public int borderWidth = 50;
        @ConfigEntry.ColorPicker
        public int borderColour = 0xbada55;
    }

    @Override
    public void validatePostLoad() {
        generalOptions.borderColour = MathHelper.clamp(generalOptions.borderColour, 0, 0xffffff);
        generalOptions.borderWidth = MathHelper.clamp(generalOptions.borderWidth, 1, 200);
        generalOptions.displayRange = MathHelper.clamp(generalOptions.displayRange, 0, 64);
    }
}
