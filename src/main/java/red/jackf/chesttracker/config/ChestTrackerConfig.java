package red.jackf.chesttracker.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import red.jackf.chesttracker.ChestTracker;

import java.util.Arrays;
import java.util.Comparator;
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
    }

    @Override
    public void validatePostLoad() {
        trackedScreens.blocklist.sort(Comparator.naturalOrder());
    }
}
