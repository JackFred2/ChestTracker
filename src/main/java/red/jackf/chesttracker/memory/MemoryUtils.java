package red.jackf.chesttracker.memory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;

import java.util.*;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public abstract class MemoryUtils {
    @Nullable
    private static BlockPos latestPos = null;

    public static <T extends ScreenHandler> void handleItemsFromScreen(@NotNull HandledScreen<T> screen) {
        if (validScreenToTrack(screen)) {
            MinecraftClient mc = MinecraftClient.getInstance();
            MemoryDatabase database = MemoryDatabase.getCurrent();
            if (database != null && mc.world != null && latestPos != null) {
                List<ItemStack> stacks = new ArrayList<>();
                screen.getScreenHandler().slots.stream().filter(Slot::hasStack).filter(slot -> !(slot.inventory instanceof PlayerInventory)).map(Slot::getStack).forEach(newStack -> {
                    boolean exists = false;
                    for (ItemStack oldStack : stacks) {
                        if (areStacksEquivalent(newStack, oldStack, false)) {
                            oldStack.setCount(oldStack.getCount() + newStack.getCount());
                            exists = true;
                        }
                    }
                    if (!exists) stacks.add(newStack);
                });
                Text title = getTitleFromScreen(screen, mc.world.getBlockEntity(latestPos));
                database.mergeItems(mc.world.getRegistryKey().getValue(), Memory.of(latestPos, stacks, title), getConnected(mc.world, latestPos));
            }
        }
    }

    private static Collection<BlockPos> getConnected(@NotNull World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock) {
            if (state.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
                boolean left = state.get(ChestBlock.CHEST_TYPE) == ChestType.LEFT;
                switch (state.get(ChestBlock.FACING)) {
                    case NORTH: return Collections.singleton(pos.add(left ? 1 : -1, 0, 0));
                    case SOUTH: return Collections.singleton(pos.add(left ? -1 : 1, 0, 0));
                    case WEST: return Collections.singleton(pos.add(0, 0, left ? -1 : 1));
                    case EAST: return Collections.singleton(pos.add(0, 0, left ? 1 : -1));
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

    public static void setLatestPos(@Nullable BlockPos latestPos) {
        MemoryUtils.latestPos = latestPos != null ? latestPos.toImmutable() : null;
    }

    @Nullable
    public static BlockPos getLatestPos() {
        return latestPos;
    }

    public static String getSingleplayerName(LevelStorage.Session session) {
        return makeFileSafe(session.getDirectoryName());
    }

    public static String makeFileSafe(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|&]", "_");
    }

    public static boolean areStacksEquivalent(@NotNull ItemStack stack1, @NotNull ItemStack stack2, boolean ignoreNbt) {
        boolean match = stack1.getItem() == stack2.getItem() && (ignoreNbt || Objects.equals(stack1.getTag(), stack2.getTag()));
        /*System.out.println("match: " + match);
        System.out.println("ignore nbt: " + ignoreNbt);
        System.out.println(stack1);
        System.out.println(stack1.getTag());
        System.out.println(stack2);
        System.out.println(stack2.getTag());*/
        return match;
    }
}
