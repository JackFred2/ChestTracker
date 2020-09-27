package red.jackf.chesttracker;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.compat.REIPlugin;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.NameEditButton;
import red.jackf.chesttracker.gui.OpenItemListButton;
import red.jackf.chesttracker.memory.Memory;
import red.jackf.chesttracker.memory.MemoryDatabase;
import red.jackf.chesttracker.memory.MemoryUtils;
import red.jackf.chesttracker.mixins.AccessorHandledScreen;
import red.jackf.chesttracker.render.RenderUtils;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ChestTracker implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("ChestTracker");
    public static final String MODID = "chesttracker";
    public static final KeyBinding SEARCH_KEY = new KeyBinding("key." + MODID + ".searchforitem", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.categories.inventory");
    public static final ChestTrackerConfig CONFIG = AutoConfig.register(ChestTrackerConfig.class, JanksonConfigSerializer::new).getConfig();

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    public static void sendDebugMessage(PlayerEntity player, Text text) {
        player.sendSystemMessage(new LiteralText("[ChestTracker] ").formatted(Formatting.YELLOW).append(text), Util.NIL_UUID);
    }

    public static void searchForItem(ItemStack stack, @NotNull World world) {
        MemoryDatabase database = MemoryDatabase.getCurrent();
        if (database != null) {
            List<Memory> found = database.findItems(stack, world.getRegistryKey().getValue());
            if (found.size() >= 1) {
                RenderUtils.addRenderPositions(found, world.getTime());
                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().player.closeHandledScreen();
            }
        }
    }

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(SEARCH_KEY);

        // Save if someone just decides to X out of craft
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MemoryDatabase database = MemoryDatabase.getCurrent();
            if (database != null) database.save();
        }, "ChestTrackerSavingThread"));

        // Checking for memories that are still alive
        ClientTickEvents.END_WORLD_TICK.register(MemoryUtils::checkValidCycle);

        ClothClientHooks.SCREEN_KEY_PRESSED.register((client, screen, keyCode, scanCode, modifiers) -> {
            if (SEARCH_KEY.matchesKey(keyCode, scanCode)) {
                MinecraftClient mc = MinecraftClient.getInstance();
                World world = mc.world;
                if (world != null && mc.currentScreen instanceof AccessorHandledScreen) {
                    // Try the current screen inventory slots first
                    Slot hovered = ((AccessorHandledScreen) mc.currentScreen).getFocusedSlot();
                    if (hovered != null && hovered.hasStack()) {
                        ChestTracker.searchForItem(hovered.getStack(), world);
                    } else if (FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
                        double scaleFactor = (double) mc.getWindow().getScaledWidth() / (double) mc.getWindow().getWidth();
                        ItemStack stack = REIPlugin.tryFindItem(mc.mouse.getX() * scaleFactor, mc.mouse.getY() * scaleFactor);
                        if (!stack.isEmpty()) ChestTracker.searchForItem(stack, world);
                    }
                }
            }

            return ActionResult.PASS;
        });

        ClothClientHooks.SCREEN_INIT_POST.register((minecraftClient, screen, screenHooks) -> {
            if (screen instanceof HandledScreen) {
                screenHooks.cloth$addButtonWidget(new OpenItemListButton((HandledScreen<?>) screen));
                if (MemoryUtils.getLatestPos() != null && !(screen instanceof AbstractInventoryScreen)) {
                    //screenHooks.cloth$addButtonWidget(new NameEditButton((HandledScreen<?>) screen));
                    //screenHooks.cloth$addButtonWidget(new FavouriteButton((HandledScreen<?>) screen));
                }
            }
        });

        UseBlockCallback.EVENT.register((playerEntity, world, hand, blockHitResult) -> {
            if (world.isClient) {
                Block hit = world.getBlockState(blockHitResult.getBlockPos()).getBlock();
                if (MemoryUtils.isValidInventoryHolder(hit, world, blockHitResult.getBlockPos())) {
                    MemoryUtils.setLatestPos(blockHitResult.getBlockPos());
                } else {
                    MemoryUtils.setLatestPos(null);
                }
            }
            return ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient) {
                MemoryUtils.setLatestPos(null);
            }
            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient) {
                MemoryUtils.setLatestPos(null);
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        });
    }
}
