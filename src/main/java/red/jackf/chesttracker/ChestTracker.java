package red.jackf.chesttracker;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.FavouriteButton;
import red.jackf.chesttracker.gui.OpenItemListButton;
import red.jackf.chesttracker.render.RenderUtils;

import java.util.*;
import java.util.stream.Stream;

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

        ClothClientHooks.SCREEN_KEY_PRESSED.register((client, screen, keyCode, scanCode, modifiers) -> {
            if (SEARCH_KEY.matchesKey(keyCode, scanCode)) {

            }

            return ActionResult.PASS;
        });

        ClothClientHooks.SCREEN_INIT_POST.register((minecraftClient, screen, screenHooks) -> {
            if (screen instanceof HandledScreen) {
                screenHooks.cloth$addButtonWidget(new OpenItemListButton((HandledScreen<?>) screen));
                screenHooks.cloth$addButtonWidget(new FavouriteButton((HandledScreen<?>) screen));

            }
        });

        UseBlockCallback.EVENT.register((playerEntity, world, hand, blockHitResult) -> {
            if (world.isClient) {
                if (Screen.hasShiftDown()) {

                } else {
                    RenderUtils.addRenderPositions(Collections.singleton(blockHitResult.getBlockPos()), world.getTime());
                }
            }
            return ActionResult.PASS;
        });
    }
}
