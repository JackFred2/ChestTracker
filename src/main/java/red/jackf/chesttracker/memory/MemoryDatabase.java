package red.jackf.chesttracker.memory;

import com.google.common.collect.Streams;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.gui.ItemListScreen;
import red.jackf.chesttracker.mixins.AccessorMinecraftServer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class MemoryDatabase {
    private final String id;
    private Map<Identifier, List<Memory>> locations;

    @Nullable
    private static MemoryDatabase currentDatabase = null;

    @Nullable
    public static MemoryDatabase getCurrent() {
        String id = getId();
        if (id == null) return null;
        MemoryDatabase database = new MemoryDatabase(id);
        database.load();
        ChestTracker.LOGGER.info("Loaded " + id);
        return database;
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

    public void save() {
        Path savePath = getFilePath();
    }

    public void load() {
        Path loadPath = getFilePath();
    }

    @NotNull
    public Path getFilePath() {
        return FabricLoader.getInstance().getGameDir().resolve("chesttracker").resolve(id + ".json");
    }

    private MemoryDatabase(String id) {
        this.id = id;
    }

    public List<ItemStack> getItems(Identifier worldId) {
        if (locations.containsKey(worldId)) {
            Map<LightweightStack, Integer> count = new HashMap<>();
            locations.get(worldId).forEach(memory -> memory.getItems().forEach(stack -> {
                LightweightStack lightweightStack = new LightweightStack(stack.getItem(), stack.getTag());
                count.merge(lightweightStack, stack.getCount(), Integer::sum);
            }));
            List<ItemStack> results = new ArrayList<>();
            count.forEach(((lightweightStack, integer) -> {
                ItemStack stack = new ItemStack(lightweightStack.getItem(), integer);
                stack.setTag(lightweightStack.getTag());
                results.add(stack);
            }));
            return results;
        } else {
            return Collections.emptyList();
        }
    }

    public static boolean areStacksEquivalent(@NotNull ItemStack stack1, @NotNull ItemStack stack2, boolean compareNbt) {
        return stack1.getItem() == stack2.getItem() && (compareNbt || Objects.equals(stack1.getTag(), stack2.getTag()));
    }
}
