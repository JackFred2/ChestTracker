package red.jackf.chesttracker;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.compat.REIPlugin;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.FavouriteButton;
import red.jackf.chesttracker.gui.ManagerButton;
import red.jackf.chesttracker.mixins.ChestTrackerAccessorHandledScreen;
import red.jackf.chesttracker.tracker.Tracker;

@Environment(EnvType.CLIENT)
public class ChestTracker implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "chesttracker";

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    public static void sendDebugMessage(PlayerEntity player, Text text) {
        player.sendSystemMessage(new LiteralText("[ChestTracker] ").formatted(Formatting.YELLOW).append(text), Util.NIL_UUID);
    }

    public static final KeyBinding SEARCH_KEY = new KeyBinding(id("search_for_items").toString(), InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.categories.inventory");

    public static ChestTrackerConfig CONFIG = AutoConfig.register(ChestTrackerConfig.class, GsonConfigSerializer::new).getConfig();

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(SEARCH_KEY);

        ManagerButton.setup();
        FavouriteButton.setup();

        ClothClientHooks.SCREEN_KEY_PRESSED.register((client, screen, keyCode, scanCode, modifiers) -> {
            if (SEARCH_KEY.matchesKey(keyCode, scanCode) && client.player != null && client.world != null) {
                ItemStack toFind = ChestTracker.tryFindItems(screen);
                if (toFind == ItemStack.EMPTY) return ActionResult.PASS;

                return Tracker.getInstance().searchForItem(toFind);
            }
            return ActionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            Tracker.getInstance().handleInteract(player, world, hand, hitResult);
            return ActionResult.PASS;
        });
    }

    @NotNull
    public static <T extends ScreenHandler> ItemStack tryFindItems(Screen screen) {
        ItemStack item = ItemStack.EMPTY;
        if (screen instanceof HandledScreen) {
            @SuppressWarnings("unchecked")
            HandledScreen<T> handledScreen = (HandledScreen<T>) screen;
            Slot slot = ((ChestTrackerAccessorHandledScreen) handledScreen).getFocusedSlot();
            if (slot != null && slot.hasStack()) item = slot.getStack();
        }

        if (item == ItemStack.EMPTY && FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
            double gameScale = (double) MinecraftClient.getInstance().getWindow().getScaledWidth() / (double) MinecraftClient.getInstance().getWindow().getWidth();
            double mouseX = MinecraftClient.getInstance().mouse.getX() * gameScale;
            double mouseY = MinecraftClient.getInstance().mouse.getY() * gameScale;
            item = REIPlugin.tryFindItem(mouseX, mouseY);
        }
        assert item != null;
        return item;
    }
}
