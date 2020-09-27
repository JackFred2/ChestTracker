package red.jackf.chesttracker.memory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;

import java.util.*;
import java.util.stream.Collectors;

import static red.jackf.chesttracker.ChestTracker.id;

@Environment(EnvType.CLIENT)
public abstract class MemoryUtils {
    public static final Identifier ENDER_CHEST_ID = id("ender_chest");
    @Nullable
    private static BlockPos latestPos = null;
    @Nullable
    private static RealmsServer lastRealmsServer = null;

    private static int ticksPerCheck = 20;
    private static List<Memory> currentlyCheckedMemories = new ArrayList<>();
    private static int currentlyCheckedIndex = 0;
    private static Identifier currentlyCheckedWorldId = null;

    public static <T extends ScreenHandler> void handleItemsFromScreen(@NotNull HandledScreen<T> screen) {
        if (validScreenToTrack(screen)) {
            MinecraftClient mc = MinecraftClient.getInstance();
            MemoryDatabase database = MemoryDatabase.getCurrent();
            if (database != null && mc.world != null && latestPos != null) {
                List<ItemStack> stacks = condenseItems(screen.getScreenHandler().slots.stream().filter(Slot::hasStack).filter(slot -> !(slot.inventory instanceof PlayerInventory)).map(Slot::getStack).collect(Collectors.toList()));
                BlockState state = mc.world.getBlockState(latestPos);
                if (state.getBlock() == Blocks.ENDER_CHEST) {
                    database.mergeItems(MemoryUtils.ENDER_CHEST_ID, Memory.of(BlockPos.ORIGIN, stacks, null, null));
                } else {
                    Text title = getTitleFromScreen(screen, mc.world.getBlockEntity(latestPos));
                    Collection<BlockPos> connected = getConnected(mc.world, latestPos);
                    database.mergeItems(mc.world.getRegistryKey().getValue(), Memory.of(latestPos, stacks, title, connected.size() > 0 ? getAveragePos(latestPos, connected) : null), connected);
                }
            }
        }
    }

    public static List<ItemStack> condenseItems(List<ItemStack> list) {
        List<ItemStack> stacks = new ArrayList<>();
        list.forEach(newStack -> {
            boolean exists = false;
            for (ItemStack oldStack : stacks) {
                if (areStacksEquivalent(newStack, oldStack, false)) {
                    oldStack.setCount(oldStack.getCount() + newStack.getCount());
                    exists = true;
                }
            }
            if (!exists) stacks.add(newStack);
        });
        return stacks;
    }

    private static Vec3d getAveragePos(BlockPos basePos, Collection<BlockPos> connected) {
        Vec3d base = Vec3d.of(basePos);
        for (BlockPos pos : connected) {
            base = base.add(Vec3d.of(pos));
        }
        return base.multiply(1f / (1 + connected.size())).subtract(Vec3d.of(basePos));
    }

    private static Collection<BlockPos> getConnected(@NotNull World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock) {
            if (state.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
                boolean left = state.get(ChestBlock.CHEST_TYPE) == ChestType.LEFT;
                switch (state.get(ChestBlock.FACING)) {
                    case NORTH:
                        return Collections.singleton(pos.add(left ? 1 : -1, 0, 0));
                    case SOUTH:
                        return Collections.singleton(pos.add(left ? -1 : 1, 0, 0));
                    case WEST:
                        return Collections.singleton(pos.add(0, 0, left ? -1 : 1));
                    case EAST:
                        return Collections.singleton(pos.add(0, 0, left ? 1 : -1));
                }
            }
        }
        return Collections.emptyList();
    }

    @Nullable
    private static Text getTitleFromScreen(HandledScreen<?> screen, @Nullable BlockEntity blockEntity) {
        Text title = screen.getTitle();
        if (title instanceof TranslatableText) { // Likely the default.
            return null;
        } else if (blockEntity instanceof NamedScreenHandlerFactory) {
            return title;
        } else {
            return null;
        }
    }

    private static <T extends ScreenHandler> boolean validScreenToTrack(HandledScreen<T> screen) {
        return !(screen instanceof AbstractInventoryScreen) && screen != null;
    }

    @Nullable
    public static BlockPos getLatestPos() {
        return latestPos;
    }

    public static void setLatestPos(@Nullable BlockPos latestPos) {
        MemoryUtils.latestPos = latestPos != null ? latestPos.toImmutable() : null;
    }

    public static String getSingleplayerName(LevelStorage.Session session) {
        //return makeFileSafe(session.getDirectoryName());
        return session.getDirectoryName();
    }

    public static String makeFileSafe(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|&]", "_");
    }

    public static boolean areStacksEquivalent(@NotNull ItemStack stack1, @NotNull ItemStack stack2, boolean ignoreNbt) {
        return stack1.getItem() == stack2.getItem()
            && (ignoreNbt
            || (!stack1.hasTag() && !stack2.hasTag())
            || Objects.equals(stack1.getTag(), stack2.getTag())
        );
    }

    public static void setLastRealmsServer(@Nullable RealmsServer lastRealmsServer) {
        MemoryUtils.lastRealmsServer = lastRealmsServer;
    }

    @Nullable
    public static RealmsServer getLastRealmsServer() {
        return lastRealmsServer;
    }

    public static boolean checkExistsInWorld(Memory memory) {
        return checkExistsInWorld(memory, MinecraftClient.getInstance().world);
    }

    public static boolean checkExistsInWorld(Memory memory, ClientWorld world) {
        BlockPos pos = memory.getPosition();
        if (world != null && pos != null) {
            WorldChunk chunk = world.getWorldChunk(pos);
            return chunk instanceof EmptyChunk || isValidInventoryHolder(chunk.getBlockState(pos).getBlock());
        }
        return true;
    }

    public static boolean isValidInventoryHolder(Block block) {
        return block instanceof BlockEntityProvider || block instanceof InventoryProvider;
    }

    public static void checkValidCycle(ClientWorld world) {
        if (world.getTime() % ChestTracker.CONFIG.databaseOptions.destroyedMemoryCheckInterval == 0) {
            MemoryDatabase database = MemoryDatabase.getCurrent();
            if (database != null) {
                if (!world.getRegistryKey().getValue().equals(currentlyCheckedWorldId)) {
                    currentlyCheckedWorldId = world.getRegistryKey().getValue();
                    currentlyCheckedMemories.clear();
                    currentlyCheckedIndex = 0;
                }
                if (currentlyCheckedMemories.size() == 0) {
                    currentlyCheckedMemories = new ArrayList<>(database.getAllMemories(world.getRegistryKey().getValue()));
                    currentlyCheckedIndex = currentlyCheckedMemories.size() - 1;
                }
                if (currentlyCheckedIndex >= 0) {
                    Memory memory = currentlyCheckedMemories.get(currentlyCheckedIndex);
                    if (!checkExistsInWorld(memory, world)) {
                        database.removePos(world.getRegistryKey().getValue(), memory.getPosition());
                    }
                    currentlyCheckedMemories.remove(currentlyCheckedIndex);
                    currentlyCheckedIndex--;
                }
            }
        }
    }
}
