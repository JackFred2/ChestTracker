package red.jackf.chesttracker;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import me.shedaniel.clothconfig2.impl.KeyBindingHooks;
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
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.compat.REIPlugin;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.mixins.ChestTrackerAccessorHandledScreen;
import red.jackf.chesttracker.gui.ManagerButton;
import red.jackf.chesttracker.render.RenderManager;
import red.jackf.chesttracker.tracker.InteractRememberType;
import red.jackf.chesttracker.tracker.Location;
import red.jackf.chesttracker.tracker.LocationStorage;
import red.jackf.chesttracker.tracker.Tracker;

import java.util.List;
import java.util.stream.Collectors;

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

        ClothClientHooks.SCREEN_KEY_PRESSED.register((client, screen, keyCode, scanCode, modifiers) -> {
            if (SEARCH_KEY.matchesKey(keyCode, scanCode) && client.player != null && client.world != null) {
                ItemStack toFind = ChestTracker.tryFindItems(screen);
                if (toFind == ItemStack.EMPTY) return ActionResult.PASS;

                return Tracker.getInstance().searchForItem(toFind);
            }
            return ActionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            boolean blockHasBE = world.getBlockState(hitResult.getBlockPos()).getBlock().hasBlockEntity();
            if (ChestTracker.CONFIG.miscOptions.blockInteractionType == InteractRememberType.ALL || blockHasBE) {
                Tracker.getInstance().setLastPos(hitResult.getBlockPos());
            }
            if (ChestTracker.CONFIG.miscOptions.debugPrint)
                sendDebugMessage(player, new TranslatableText("chesttracker.block_clicked_" + (blockHasBE ? "be_provider" : "not_be_provider"),
                        Registry.BLOCK.getId(world.getBlockState(hitResult.getBlockPos()).getBlock()))
                        .formatted(blockHasBE ? Formatting.GREEN : Formatting.YELLOW));
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
