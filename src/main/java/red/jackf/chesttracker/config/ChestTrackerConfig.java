package red.jackf.chesttracker.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import red.jackf.chesttracker.ChestTracker;

@Environment(EnvType.CLIENT)
@Config(name = ChestTracker.MODID)
@Config.Gui.CategoryBackground(category = "visual_options", background = "minecraft:textures/block/fire_coral_block.png")
public class ChestTrackerConfig implements ConfigData {

    @ConfigEntry.Category("visual_options")
    @ConfigEntry.Gui.TransitiveObject
    public VisualOptions visualOptions = new VisualOptions();

    @Override
    public void validatePostLoad() {
        visualOptions.borderColour = MathHelper.clamp(visualOptions.borderColour, 0, 0xffffff);
        visualOptions.fadeOutTime = MathHelper.clamp(visualOptions.fadeOutTime, 0, 300);
        visualOptions.borderWidth = MathHelper.clamp(visualOptions.borderWidth, 1, 10);
        visualOptions.nameRenderRange = MathHelper.clamp(visualOptions.nameRenderRange, 1, 16);
    }

    public static class VisualOptions {
        @ConfigEntry.BoundedDiscrete(min = 0, max = 300)
        public int fadeOutTime = 140;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int borderWidth = 8;
        @ConfigEntry.ColorPicker
        public int borderColour = 0x00baff;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 16)
        public int nameRenderRange = 8;
    }
}
