package red.jackf.chesttracker.storage;

import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.mixins.MinecraftServerAccessor;
import red.jackf.chesttracker.util.StringUtil;

/**
 * Reusable data for a game 'connection' - this connection being a world, LAN server, multiplayer server or realm.
 *
 * @param connectionId
 * @param name
 */
public record LoadContext(String connectionId, String name) {
    private static String lastRealmName = "Unknown Realm";
    private static long lastRealmId = -1L;

    /**
     * Returns a relevant ID and user facing name from the current Minecraft instance, or null if not applicable.
     */
    @Nullable
    public static LoadContext get() {
        var mc = Minecraft.getInstance();
        var connection = mc.getConnection();
        if (connection != null && connection.getConnection().isConnected()) {
            var serverData = mc.getCurrentServer();
            if (mc.getSingleplayerServer() != null) { // we dont care if we've published to LAN as the host
                // singleplayer
                return new LoadContext(
                        "singleplayer/" + StringUtil.sanitizeForPath(((MinecraftServerAccessor) mc.getSingleplayerServer()).getStorageSource()
                                                                             .getLevelId()),
                        I18n.get("menu.singleplayer") + ": " + mc.getSingleplayerServer().getWorldData().getLevelName()
                );
            // mc.getSingleplayerServer == null)
            } else if (serverData != null) {
                if (serverData.isRealm()) {
                    // realms, can't use username as ID in case of changes so just use unique(?) connectionId
                    return new LoadContext(
                            "realms/" + StringUtil.sanitizeForPath(StringUtils.leftPad(Long.toHexString(lastRealmId), 16)),
                            I18n.get("menu.online") + ": " + lastRealmName
                    );
                } else if (serverData.isLan()) {
                    // remove LAN port because while port changes a lot ip probably doesn't
                    var safeIp = serverData.ip.replaceFirst(":\\d+$", "");
                    return new LoadContext(
                            "lan/" + StringUtil.sanitizeForPath(safeIp),
                            "LAN: " + (serverData.playerList.isEmpty() ? safeIp : serverData.playerList.get(0))
                    );
                } else {
                    return new LoadContext(
                            "multiplayer/" + StringUtil.sanitizeForPath(serverData.ip),
                            I18n.get("menu.multiplayer") + ": " + serverData.name
                    );
                }
            }
        }
        return null;
    }

    public static void setLastRealm(RealmsServer server) {
        lastRealmId = server.id;
        lastRealmName = server.name;
    }
}
