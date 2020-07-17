package red.jackf.chesttracker;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.compat.REIPlugin;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.mixins.AccessorHandledScreen;

@Environment(EnvType.CLIENT)
public class ChestTracker implements ClientModInitializer {
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
                tryFindItems(client, screen);
                //client.player.closeHandledScreen();
            }
            return ActionResult.PASS;
        }));
    }

    private <T extends ScreenHandler> void tryFindItems(MinecraftClient client, Screen screen) {
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

        if (item != null)
            //noinspection ConstantConditions
            client.player.sendSystemMessage(new LiteralText("Searching for ").append(new TranslatableText(item.getTranslationKey())), Util.NIL_UUID);
    }
}
