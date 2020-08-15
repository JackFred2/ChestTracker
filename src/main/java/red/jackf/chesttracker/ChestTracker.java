package red.jackf.chesttracker;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.config.ChestTrackerConfig;

@Environment(EnvType.CLIENT)
public class ChestTracker implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "chesttracker";
    public static final KeyBinding SEARCH_KEY = new KeyBinding("key." + MODID + ".searchforitem", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.categories.inventory");
    public static ChestTrackerConfig CONFIG = AutoConfig.register(ChestTrackerConfig.class, GsonConfigSerializer::new).getConfig();

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    public static void sendDebugMessage(PlayerEntity player, Text text) {
        player.sendSystemMessage(new LiteralText("[ChestTracker] ").formatted(Formatting.YELLOW).append(text), Util.NIL_UUID);
    }

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(SEARCH_KEY);
    }
}
