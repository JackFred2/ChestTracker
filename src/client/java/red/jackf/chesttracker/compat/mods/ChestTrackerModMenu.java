package red.jackf.chesttracker.compat.mods;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import red.jackf.chesttracker.config.ChestTrackerConfigScreenBuilder;

@Environment(EnvType.CLIENT)
public class ChestTrackerModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ChestTrackerConfigScreenBuilder::build;
    }
}
