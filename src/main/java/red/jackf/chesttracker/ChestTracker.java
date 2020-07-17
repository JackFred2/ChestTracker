package red.jackf.chesttracker;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.compat.REIPlugin;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.mixins.AccessorHandledScreen;
import red.jackf.chesttracker.tracker.InteractRememberType;
import red.jackf.chesttracker.tracker.LocationStorage;
import red.jackf.chesttracker.tracker.Tracker;

@Environment(EnvType.CLIENT)
public class ChestTracker implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "chesttracker";

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    public static final KeyBinding SEARCH_KEY = new KeyBinding(id("search_for_items").toString(), InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F4, "key.categories.inventory");

    public static ChestTrackerConfig CONFIG = AutoConfig.register(ChestTrackerConfig.class, JanksonConfigSerializer::new).getConfig();

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(SEARCH_KEY);
        ClothClientHooks.SCREEN_KEY_PRESSED.register(((client, screen, keyCode, scanCode, modifiers) -> {
            if (SEARCH_KEY.matchesKey(keyCode, scanCode) && client.player != null) {
                ItemStack toFind = tryFindItems(client, screen);
                LocationStorage storage = LocationStorage.get();

            }
            return ActionResult.PASS;
        }));

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (ChestTracker.CONFIG.miscOptions.blockInterationType == InteractRememberType.ALL || world.getBlockState(hitResult.getBlockPos()).getBlock().hasBlockEntity()) {
                Tracker.getInstance().setLastPos(hitResult.getBlockPos());
            }
            return ActionResult.PASS;
        });
    }

    @Nullable
    private <T extends ScreenHandler> ItemStack tryFindItems(MinecraftClient client, Screen screen) {
        ItemStack item = null;
        if (screen instanceof HandledScreen) {
            @SuppressWarnings("unchecked")
            HandledScreen<T> handledScreen = (HandledScreen<T>) screen;
            Slot slot = ((AccessorHandledScreen) handledScreen).getFocusedSlot();
            if (slot != null && slot.hasStack()) item = slot.getStack();
        }

        if (item == null && FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
            double gameScale = (double) MinecraftClient.getInstance().getWindow().getScaledWidth() / (double) MinecraftClient.getInstance().getWindow().getWidth();
            double mouseX = MinecraftClient.getInstance().mouse.getX() * gameScale;
            double mouseY = MinecraftClient.getInstance().mouse.getY() * gameScale;
            item = REIPlugin.tryFindItem(mouseX, mouseY);
        }
        return item;
    }
}
