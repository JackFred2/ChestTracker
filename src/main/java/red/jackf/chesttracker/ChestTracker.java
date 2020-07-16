package red.jackf.chesttracker;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import red.jackf.chesttracker.config.ChestTrackerConfig;

@Environment(EnvType.CLIENT)
public class ChestTracker implements ClientModInitializer {
    public static final String MODID = "chesttracker";

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    public static ChestTrackerConfig CONFIG = AutoConfig.register(ChestTrackerConfig.class, JanksonConfigSerializer::new).getConfig();

    @Override
    public void onInitializeClient() {

    }
}
