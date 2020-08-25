package red.jackf.chesttracker.memory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;

@Environment(EnvType.CLIENT)
public abstract class MemoryUtils {
    @Nullable
    private static BlockPos latestPos = null;

    public static <T extends ScreenHandler> void handleItemsFromScreen(@NotNull HandledScreen<T> screen) {
        if (validScreenToTrack(screen)) {
            for (Slot slot : screen.getScreenHandler().slots) {
                if (slot.hasStack()) {
                    ChestTracker.LOGGER.info(slot.getStack());
                }
            }
        }
    }

    private static <T extends ScreenHandler> boolean validScreenToTrack(HandledScreen<T> screen) {
        return !(screen instanceof AbstractInventoryScreen);
    }

    public static void setLatestPos(@Nullable BlockPos latestPos) {
        MemoryUtils.latestPos = latestPos != null ? latestPos.toImmutable() : null;
    }

    public static @Nullable BlockPos getLatestPos() {
        return latestPos;
    }

    public static String getSingleplayerName(LevelStorage.Session session) {
        return makeFileSafe(session.getDirectoryName());
    }

    public static String makeFileSafe(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|&]", "_");
    }
}
