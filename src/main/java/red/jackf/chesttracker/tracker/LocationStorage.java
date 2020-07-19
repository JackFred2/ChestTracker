package red.jackf.chesttracker.tracker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.GsonHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

// Per connection storage (i.e. per single player world, server ip, realm)
public class LocationStorage {
    private static final Path ROOT_DIR = Paths.get(MinecraftClient.getInstance().runDirectory.getAbsolutePath()).resolve("chesttracker");
    private static final Gson GSON = GsonHandler.get();

    @Nullable
    private static LocationStorage currentStorage = null;

    private final String savePath;
    private Map<String, WorldStorage> storage = new HashMap<>();

    private LocationStorage(String savePath) {
        this.savePath = savePath;
        // Loading from file
        this.load();
    }

    public Path getFilePath() {
        return ROOT_DIR.resolve(savePath + ".json");
    }

    @Nullable
    public static LocationStorage get() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        String path = "";
        if (handler != null && handler.getConnection().isOpen()) {
            if (client.getServer() != null) {
                // Single player, or opened to LAN.
                path = "singleplayer-" + client.getServer().getSavePath(WorldSavePath.ROOT).getParent().getFileName();
            } else if (client.isConnectedToRealms()) { // Realms
                path = "realms-currentlyunsupportedsorry";
            } else { // Multiplayer/LAN not hosted
                path = "multiplayer-" + getUsefulFileString(handler.getConnection().getAddress());
            }
        }
        if (path.equals(""))
            return null;

        if (currentStorage == null || !currentStorage.savePath.equals(path)) {
            if (currentStorage != null) {
                currentStorage.save();
            }
            currentStorage = new LocationStorage(path);
        }

        return currentStorage;
    }

    public void closeDown() {
        this.save();
        LocationStorage.currentStorage = null;
    }

    private void save() {
        File folder = new File(ROOT_DIR.toUri());
        if (!folder.mkdirs()) {
            File targetFile = new File(getFilePath().toUri());
            try {
                FileWriter writer = new FileWriter(targetFile);
                GSON.toJson(storage, writer);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                ChestTracker.LOGGER.error("Error saving to file", e);
            }
        } else {
            ChestTracker.LOGGER.error("Could not create folder at " + folder.getAbsolutePath() + ".");
        }
    }

    private void load() {
        File folder = new File(ROOT_DIR.toUri());
        if (!folder.mkdirs()) {
            File targetFile = new File(getFilePath().toUri());
            if (targetFile.exists()) {
                try {
                    storage = GSON.fromJson(new FileReader(targetFile), new TypeToken<Map<String, WorldStorage>>() {
                    }.getType());
                } catch (FileNotFoundException e) {
                    ChestTracker.LOGGER.error("Error loading from file", e);
                }
            } else {
                ChestTracker.LOGGER.info("Creating new storage at " + targetFile);
            }
        } else {
            ChestTracker.LOGGER.error("Could not create folder at " + folder.getAbsolutePath() + ".");
        }
        if (storage == null)
            storage = new HashMap<>();
    }

    public void mergeItems(BlockPos pos, Identifier worldId, List<ItemStack> items) {
        WorldStorage storage = this.storage.computeIfAbsent(worldId.toString(), (worldRegistryKey -> new WorldStorage()));
        Location location = new Location(pos, null, items);
        storage.remove(location);
        storage.add(location);
    }

    public List<Location> findItems(Identifier worldId, ItemStack toFind) {
        WorldStorage storage = this.storage.computeIfAbsent(worldId.toString(), (worldRegistryKey -> new WorldStorage()));
        return storage.stream()
                .filter(location -> location.getItems().stream().anyMatch(itemStack -> stacksEqual(toFind, itemStack)))
                .collect(Collectors.toList());
    }

    private static boolean stacksEqual(ItemStack candidate, ItemStack toFind) {
        return candidate.getItem() == toFind.getItem()
                && (!toFind.hasTag() || toFind.getTag() == candidate.getTag());
    }

    // Per world storage
    public static class WorldStorage extends HashSet<Location> {
    }

    private static String getUsefulFileString(@NotNull SocketAddress address) {
        if (address instanceof InetSocketAddress) {
            InetSocketAddress inet = (InetSocketAddress) address;
            return inet.getAddress().getHostAddress() + (inet.getPort() == 25565 ? "" : "-" + inet.getPort());
        } else {
            return address.toString().replace(':', '-').replace('/', '-');
        }
    }

    @Override
    public String toString() {
        return "LocationStorage{" +
                "savePath='" + savePath + '\'' +
                ", storage=" + storage +
                '}';
    }
}
