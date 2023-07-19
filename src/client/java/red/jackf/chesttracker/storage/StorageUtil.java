package red.jackf.chesttracker.storage;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.memory.ItemMemory;
import red.jackf.chesttracker.mixins.MinecraftServerAccessor;
import red.jackf.chesttracker.util.StringUtil;

public class StorageUtil {
    private static long lastRealmId = -1L;

    private StorageUtil() {}

    private static Storage instance;

    public static Storage getStorage() {
        return instance;
    }

    static void setStorage(Storage storage) {
        instance = storage;
    }

    public static void setup() {
        ChestTrackerConfig.INSTANCE.getConfig().memory.storageBackend.load();

        // storage saving hooks

        // on pause
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof PauseScreen) ItemMemory.save();
        });
    }

    @Nullable
    private static String getLoadID(Minecraft mc) {
        var connection = mc.getConnection();
        if (connection != null && connection.getConnection().isConnected()) {
            var currentServer = mc.getCurrentServer();
            if (mc.getSingleplayerServer() != null) { // we dont care if we've published to LAN
                // singleplayer
                return "singleplayer/" + StringUtil.sanitize(((MinecraftServerAccessor) mc.getSingleplayerServer()).getStorageSource().getLevelId());
            } else if (mc.isConnectedToRealms()) {
                // realms
                return "realms/" + StringUtil.sanitize(StringUtils.leftPad(Long.toHexString(lastRealmId), 16));
            } else if (mc.getSingleplayerServer() == null && currentServer != null) {
                if (currentServer.isLan())
                    // remove LAN port because while port changes a lot ip probably doesn't
                    return "lan/" + StringUtil.sanitize(currentServer.ip.replaceFirst(":\\d+$", ""));
                else
                    return "multiplayer/" +  StringUtil.sanitize(currentServer.ip);
            }
        }
        return null;
    }

    /**
     * Load the appropriate memory based on the current context
     */
    public static void load(Minecraft mc) {
        var path = getLoadID(mc);
        ChestTracker.LOGGER.debug("Path: {}", path);
        if (path == null) ItemMemory.unload();
        else ItemMemory.load(path);
    }

    public static void setLastRealmID(long lastRealmId) {
        StorageUtil.lastRealmId = lastRealmId;
    }
}
