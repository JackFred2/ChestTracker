package red.jackf.chesttracker.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.util.math.MathHelper;
import red.jackf.chesttracker.ChestTracker;

import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
@Config(name = ChestTracker.MODID)
@Config.Gui.CategoryBackground(category = "visual_options", background = "minecraft:textures/block/fire_coral_block.png")
@Config.Gui.CategoryBackground(category = "tracked_guis", background = "minecraft:textures/block/brain_coral_block.png")
@Config.Gui.CategoryBackground(category = "misc_options", background = "minecraft:textures/block/tube_coral_block.png")
public class ChestTrackerConfig implements ConfigData {

    @ConfigEntry.Category("misc_options")
    @ConfigEntry.Gui.TransitiveObject
    public MiscOptions miscOptions = new MiscOptions();

    @ConfigEntry.Category("visual_options")
    @ConfigEntry.Gui.TransitiveObject
    public VisualOptions visualOptions = new VisualOptions();

    @ConfigEntry.Category("tracked_guis")
    @ConfigEntry.Gui.TransitiveObject
    public TrackedScreens trackedScreens = new TrackedScreens();

    public static class MiscOptions {
        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public InteractRememberType blockInteractionType = InteractRememberType.BLOCK_ENTITIES;
        public Boolean debugPrint = false;
    }

    public static class VisualOptions {
        @ConfigEntry.BoundedDiscrete(min = 0, max = 300)
        public int fadeOutTime = 140;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int borderWidth = 8;
        @ConfigEntry.ColorPicker
        public int borderColour = 0x00baff;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 256)
        public int borderRenderRange = 64;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 16)
        public int nameRenderRange = 8;
    }

    public static class TrackedScreens {
        @ConfigEntry.Gui.PrefixText
        public List<String> blocklist = Arrays.asList(
            AnvilScreen.class.getSimpleName(),
            BeaconScreen.class.getSimpleName(),
            CartographyTableScreen.class.getSimpleName(),
            CraftingScreen.class.getSimpleName(),
            CreativeInventoryScreen.class.getSimpleName(),
            EnchantmentScreen.class.getSimpleName(),
            GrindstoneScreen.class.getSimpleName(),
            HopperScreen.class.getSimpleName(),
            HorseScreen.class.getSimpleName(),
            InventoryScreen.class.getSimpleName(),
            LoomScreen.class.getSimpleName(),
            MerchantScreen.class.getSimpleName(),
            SmithingScreen.class.getSimpleName(),
            StonecutterScreen.class.getSimpleName()
        );
    }

    @Override
    public void validatePostLoad() {
        visualOptions.borderColour = MathHelper.clamp(visualOptions.borderColour, 0, 0xffffff);
        visualOptions.fadeOutTime = MathHelper.clamp(visualOptions.fadeOutTime, 0, 300);
        visualOptions.borderWidth = MathHelper.clamp(visualOptions.borderWidth, 1, 10);
        visualOptions.nameRenderRange = MathHelper.clamp(visualOptions.nameRenderRange, 1, 16);
        visualOptions.borderRenderRange = MathHelper.clamp(visualOptions.borderRenderRange, 1, 256);
    }
}
