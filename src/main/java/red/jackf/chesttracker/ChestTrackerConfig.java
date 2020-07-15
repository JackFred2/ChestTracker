package red.jackf.chesttracker;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
@Config(name=ChestTracker.MODID)
public class ChestTrackerConfig implements ConfigData {
    boolean foo = false;

    @ConfigEntry.BoundedDiscrete(min=0, max=60)
    int bar = 0;

    @Override
    public void validatePostLoad() throws ValidationException {
        bar = MathHelper.clamp(bar, 0, 60);
    }
}
