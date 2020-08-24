package red.jackf.chesttracker.memory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.mixins.AccessorMinecraftServer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

public class MemoryDatabase {
    private final String name;
    private Map<Identifier, List<Memory>> locations;

    @Nullable
    public static MemoryDatabase getCurrent() {
        String id = getId();
        return null;
    }

    @Nullable
    private static String getId() {
        MinecraftClient mc = MinecraftClient.getInstance();
        String id = null;
        if (mc.isInSingleplayer() && mc.getServer() != null) {
            id = "singleplayer-" + MemoryUtils.getSingleplayerName(((AccessorMinecraftServer) mc.getServer()).getSession());
        } else if (mc.isConnectedToRealms()) {
            id = "realms-canonlysupport1rightnowsorry";
        } else {
            ClientPlayNetworkHandler cpnh = mc.getNetworkHandler();
            if (cpnh != null) {
                SocketAddress address = cpnh.getConnection().getAddress();
                if (address instanceof InetSocketAddress) {
                    InetSocketAddress inet = ((InetSocketAddress) address);
                    id = "multiplayer-" + inet.getAddress() + (inet.getPort() == 25565 ? "" : "-" + inet.getPort());
                } else {
                    id = "multiplayer-" + MemoryUtils.makeFileSafe(address.toString());
                }
            }
        }

        return id;
    }

    private MemoryDatabase(String name) {
        this.name = name;
    }

}
