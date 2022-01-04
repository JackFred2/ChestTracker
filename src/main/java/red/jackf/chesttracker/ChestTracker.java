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
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
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
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.ChestTrackerButtonWidget;
import red.jackf.chesttracker.gui.ItemListScreen;
import red.jackf.chesttracker.memory.Memory;
import red.jackf.chesttracker.memory.MemoryDatabase;
import red.jackf.chesttracker.memory.MemoryUtils;
import red.jackf.chesttracker.resource.ButtonPositionManager;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.WhereIsItClient;
import red.jackf.whereisit.client.PositionData;
import red.jackf.whereisit.client.RenderUtils;
import red.jackf.whereisit.compat.OptifineHooks;

import java.util.Collection;

@Environment(EnvType.CLIENT)
public class ChestTracker implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("ChestTracker");
    public static final String MODID = "chesttracker";
    public static final KeyBinding GUI_KEY = new KeyBinding("key." + MODID + ".opengui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories." + MODID);
    public static final ChestTrackerConfig CONFIG = AutoConfig.register(ChestTrackerConfig.class, JanksonConfigSerializer::new).getConfig();

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    public static void sendDebugMessage(Text text) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null)
            player.sendSystemMessage(new LiteralText("[ChestTracker] ").formatted(Formatting.YELLOW).append(text), Util.NIL_UUID);
    }

    public static void searchForItem(@NotNull ItemStack stack) {
        var database = MemoryDatabase.getCurrent();
        var client = MinecraftClient.getInstance();
        var world = client.world;
        if (database != null && world != null) {
            startRenderingForLocations(database.findItems(stack, world.getRegistryKey().getValue()));
        }
    }

    public static void startRenderingForLocations(Collection<Memory> memories) {
        var client = MinecraftClient.getInstance();
        var world = client.world;
        var player = client.player;
        if (world != null && memories.size() >= 1) {
            float r = ((ChestTracker.CONFIG.visualOptions.borderColour >> 16) & 0xff) / 255f;
            float g = ((ChestTracker.CONFIG.visualOptions.borderColour >> 8) & 0xff) / 255f;
            float b = ((ChestTracker.CONFIG.visualOptions.borderColour) & 0xff) / 255f;
            WhereIsItClient.handleFoundItems(memories.stream()
                .map(memory -> new PositionData(memory.getPosition(), world.getTime(), VoxelShapes.fullCube(), r, g, b, null))
                .toList());
            if (player != null)
                player.closeHandledScreen();
        }
    }

    public static int getSquareSearchRange() {
        int blockValue = sliderValueToRange(ChestTracker.CONFIG.miscOptions.searchRange);
        if (blockValue == Integer.MAX_VALUE) return blockValue;
        return blockValue * blockValue;
    }

    public static int sliderValueToRange(int sliderValue) {
        if (sliderValue <= 16) {
            return 15 + sliderValue;
        } else if (sliderValue <= 32) {
            return 30 + ((sliderValue - 16) * 2);
        } else if (sliderValue <= 48) {
            return 60 + ((sliderValue - 32) * 4);
        } else if (sliderValue <= 64) {
            return 120 + ((sliderValue - 48) * 8);
        } else if (sliderValue <= 80) {
            return 240 + ((sliderValue - 64) * 16);
        } else if (sliderValue <= 97) {
            return 480 + ((sliderValue - 80) * 32);
        }
        return Integer.MAX_VALUE;
    }

    public static void drawLabels(WorldRenderContext context) {
        if (ChestTracker.CONFIG.visualOptions.nameRenderRange == 0) return;
        MemoryDatabase database = MemoryDatabase.getCurrent();
        if (database == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world != null) {
            // Named nearby
            Collection<Memory> toRender = database.getNamedMemories(mc.world.getRegistryKey().getValue());
            for (Memory memory : toRender) {
                BlockPos blockPos = memory.getPosition();
                boolean alreadyHandled = WhereIsIt.CONFIG.shouldShowResultLabels() && RenderUtils.FOUND_ITEM_POSITIONS.containsKey(blockPos);
                if (blockPos != null && memory.getTitle() != null && !alreadyHandled) {
                    Vec3d pos = Vec3d.of(blockPos);
                    if (memory.getNameOffset() != null) pos = pos.add(memory.getNameOffset());
                    if (!context.world().getBlockState(blockPos.up()).isOpaque()) pos = pos.add(0, 1d, 0);
                    RenderUtils.drawTextWithBackground(context, pos, memory.getTitle(), CONFIG.visualOptions.nameRenderRange, true);
                }
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

        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hitResult) -> {
            OptifineHooks.doOptifineAwareRender(context, (context1, simple) -> {
                ChestTracker.drawLabels(context1);
            });
            return true;
        });

        WhereIsItClient.SEARCH_FOR_ITEM.register((item, matchNbt, compoundTag) -> {
            ItemStack stack = new ItemStack(item);
            if (matchNbt) stack.setNbt(compoundTag);
            searchForItem(stack);
        });

        // Opening GUI
        ClientTickEvents.START_CLIENT_TICK.register((client) -> {
            if (GUI_KEY.wasPressed() && client.world != null && client.currentScreen == null) {
                client.setScreen(new ItemListScreen());
            }
        });

        // Checking for memories that are still alive
        ClientTickEvents.END_WORLD_TICK.register(MemoryUtils::checkValidCycle);

        // JSON Button Positions
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new ButtonPositionManager());

        // ChestTracker GUI button
        ClothClientHooks.SCREEN_INIT_POST.register((minecraftClient, screen, screenHooks) -> {
            if (screen instanceof HandledScreen) {
                if (ChestTracker.CONFIG.visualOptions.enableButton) {
                    screenHooks.cloth$addDrawableChild(new ChestTrackerButtonWidget((HandledScreen<?>) screen, shouldDeleteBeEnabled()));
                }
            }
        });

        UseBlockCallback.EVENT.register((playerEntity, world, hand, blockHitResult) -> {
            if (world.isClient) {
                Block hit = world.getBlockState(blockHitResult.getBlockPos()).getBlock();
                if (MemoryUtils.isValidInventoryHolder(hit, world, blockHitResult.getBlockPos())) {
                    MemoryUtils.setLatestPos(blockHitResult.getBlockPos());
                    MemoryUtils.setWasEnderchest(hit == Blocks.ENDER_CHEST);
                } else {
                    MemoryUtils.setLatestPos(null);
                    MemoryUtils.setWasEnderchest(false);
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

    private boolean shouldDeleteBeEnabled() {
        return MemoryUtils.getLatestPos() != null
            && !(MinecraftClient.getInstance().currentScreen instanceof AbstractInventoryScreen);
    }
}
