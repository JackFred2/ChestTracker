package red.jackf.chesttracker;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.ItemListScreen;
import red.jackf.chesttracker.gui.ButtonWidgets;
import red.jackf.chesttracker.memory.Memory;
import red.jackf.chesttracker.memory.MemoryDatabase;
import red.jackf.chesttracker.memory.MemoryUtils;
import red.jackf.chesttracker.mixins.AccessorHandledScreen;
import red.jackf.chesttracker.render.RenderUtils;
import red.jackf.chesttracker.resource.ButtonPositionManager;
import red.jackf.whereisit.WhereIsItClient;
import red.jackf.whereisit.client.FoundItemPos;

import java.util.List;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class ChestTracker implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("ChestTracker");
    public static final String MODID = "chesttracker";
    public static final KeyBinding GUI_KEY = new KeyBinding("key." + MODID + ".opengui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories." + MODID);
    public static final ChestTrackerConfig CONFIG = AutoConfig.register(ChestTrackerConfig.class, JanksonConfigSerializer::new).getConfig();

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    public static void sendDebugMessage(PlayerEntity player, Text text) {
        player.sendSystemMessage(new LiteralText("[ChestTracker] ").formatted(Formatting.YELLOW).append(text), Util.NIL_UUID);
    }

    public static void searchForItem(@NotNull ItemStack stack, @NotNull World world) {
        MemoryDatabase database = MemoryDatabase.getCurrent();
        if (database != null) {
            List<Memory> found = database.findItems(stack, world.getRegistryKey().getValue());
            if (found.size() >= 1) {
                float r = ((ChestTracker.CONFIG.visualOptions.borderColour >> 16) & 0xff) / 255f;
                float g = ((ChestTracker.CONFIG.visualOptions.borderColour >> 8) & 0xff) / 255f;
                float b = ((ChestTracker.CONFIG.visualOptions.borderColour) & 0xff) / 255f;
                WhereIsItClient.handleFoundItems(found.stream()
                    .map(memory -> new FoundItemPos(memory.getPosition(), world.getTime(), VoxelShapes.fullCube(), r, g, b))
                    .collect(Collectors.toList()));
                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().player.closeHandledScreen();
            }
        }
    }

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(GUI_KEY);

        // Save if someone just decides to X out of craft
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            MemoryDatabase database = MemoryDatabase.getCurrent();
            if (database != null) database.save();
        });

        WorldRenderEvents.LAST.register(RenderUtils::draw);

        WhereIsItClient.SEARCH_FOR_ITEM.register((item, matchNbt, compoundTag) -> {
            if (MinecraftClient.getInstance().world != null) {
                ItemStack stack = new ItemStack(item);
                if (matchNbt) stack.setTag(compoundTag);
                searchForItem(stack, MinecraftClient.getInstance().world);
            }
        });

        // Opening GUI
        ClientTickEvents.START_CLIENT_TICK.register((client) -> {
            if (GUI_KEY.wasPressed() && client.world != null) {
                if (client.currentScreen != null) client.currentScreen.onClose();
                client.openScreen(new ItemListScreen());
            }
        });

        // Checking for memories that are still alive
        ClientTickEvents.END_WORLD_TICK.register(MemoryUtils::checkValidCycle);

        // JSON Button Positions
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new ButtonPositionManager());

        // Find hotkeys
        ClothClientHooks.SCREEN_KEY_RELEASED.register((mc, currentScreen, keyCode, scanCode, modifiers) -> {
            if (GUI_KEY.matchesKey(keyCode, scanCode)) {
                if (currentScreen instanceof HandledScreen && !(currentScreen instanceof CreativeInventoryScreen && ((CreativeInventoryScreen) currentScreen).getSelectedTab() == ItemGroup.SEARCH.getIndex())) {
                    currentScreen.onClose();
                    mc.openScreen(new ItemListScreen());
                }
            }

            return ActionResult.PASS;
        });

        // ChestTracker GUI button
        ClothClientHooks.SCREEN_INIT_POST.register((minecraftClient, screen, screenHooks) -> {
            if (screen instanceof HandledScreen) {
                if (ChestTracker.CONFIG.visualOptions.enableButton) {
                    screenHooks.cloth$addDrawableChild(new ButtonWidgets((HandledScreen<?>) screen));
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
