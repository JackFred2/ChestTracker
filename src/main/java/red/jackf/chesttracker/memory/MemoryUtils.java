package red.jackf.chesttracker.memory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
                    System.out.println("New " + newStack);
                    boolean exists = false;
                    for (ItemStack oldStack : stacks) {
                        if (areStacksEquivalent(newStack, oldStack, false)) {
                            oldStack.setCount(oldStack.getCount() + newStack.getCount());
                            exists = true;
                        }
                    }
                    if (!exists) stacks.add(newStack);
                });
                database.mergeItems(mc.world.getRegistryKey().getValue(), Memory.of(latestPos, stacks, null));
            }
        }
    }

    private static <T extends ScreenHandler> boolean validScreenToTrack(HandledScreen<T> screen) {
        return !(screen instanceof AbstractInventoryScreen);
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
        return stack1.getItem() == stack2.getItem() && (ignoreNbt || Objects.equals(stack1.getTag(), stack2.getTag()));
    }
}
