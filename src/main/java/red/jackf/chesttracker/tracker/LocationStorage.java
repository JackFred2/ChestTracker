package red.jackf.chesttracker.tracker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
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

    public String getSavePath() {
        return savePath;
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
        System.out.println(this);
    }

    // Per world storage
    public static class WorldStorage extends HashSet<Location> {
    }

    public static class ServerStorage extends HashMap<Identifier, Location> {
    }

    public static class Location {
        private final BlockPos position;
        @Nullable
        private Text name;
        private List<ItemStack> items;

        public Location(BlockPos position, @Nullable Text name, List<ItemStack> items) {
            this.position = position;
            this.name = name;
            this.items = items;
        }

        public BlockPos getPosition() {
            return position;
        }

        @Nullable
        public Text getName() {
            return name;
        }

        public void setName(@Nullable Text name) {
            this.name = name;
        }

        public List<ItemStack> getItems() {
            return items;
        }

        public void setItems(List<ItemStack> items) {
            this.items = items;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Location location = (Location) o;
            return position.equals(location.position);
        }

        @Override
        public int hashCode() {
            return Objects.hash(position);
        }

        @Override
        public String toString() {
            return "Location{" +
                    "position=" + position +
                    ", name=" + name +
                    ", items=" + items +
                    '}';
        }
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
