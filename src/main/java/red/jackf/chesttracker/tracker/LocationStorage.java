package red.jackf.chesttracker.tracker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
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

    public void mergeItems(BlockPos pos, World world, List<ItemStack> items, Text title, Boolean favourite) {
        WorldStorage storage = this.storage.computeIfAbsent(world.getRegistryKey().getValue().toString(), (worldRegistryKey -> new WorldStorage()));
        List<BlockPos> positions = LinkedBlocksHandler.getLinked(world, pos);
        Vec3d offset = centerOf(positions).subtract(Vec3d.of(pos));
        Location location = new Location(pos, title instanceof TranslatableText ? null : title, positions.size() == 1 ? null : offset, items, favourite);

        storage.removeAll(positions.stream()
            .map(storage.lookupMap::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));

        storage.add(location);
    }

    private static Vec3d centerOf(List<BlockPos> positions) {
        Vec3d result = Vec3d.ZERO;
        for (BlockPos pos : positions) {
            result = result.add(Vec3d.of(pos));
        }
        return result.multiply(1d/positions.size());
    }

    public List<Location> findItems(Identifier worldId, ItemStack toFind) {
        WorldStorage storage = getStorage(worldId);
        List<Location> results = storage.stream()
                .filter(location -> location.getItems().stream().anyMatch(itemStack -> stacksEqual(toFind, itemStack)))
                .collect(Collectors.toList());
        storage.verifyItems(results);

        return results;
    }

    public WorldStorage getStorage(Identifier worldId) {
        return this.storage.computeIfAbsent(worldId.toString(), (worldRegistryKey -> new WorldStorage()));
    }

    private static boolean stacksEqual(ItemStack candidate, ItemStack toFind) {
        return candidate.getItem() == toFind.getItem();
                //&& (!toFind.hasTag() || Objects.equals(toFind.getTag(), candidate.getTag()));
    }

    // Per world storage
    public static class WorldStorage extends HashSet<Location> {
        private final Map<BlockPos, Location> lookupMap = new HashMap<>();

        @Override
        public boolean remove(Object o) {
            lookupMap.remove(o);
            return super.remove(o);
        }

        @Override
        public void clear() {
            lookupMap.clear();
            super.clear();
        }

        public Location lookupFast(BlockPos pos) {
            return lookupMap.get(pos);
        }

        @Override
        public boolean add(Location location) {
            lookupMap.put(location.getPosition(), location);
            return super.add(location);
        }

        public void verifyItems(Collection<Location> list) {
            if (MinecraftClient.getInstance().world != null) {
                for (Iterator<Location> iterator = list.iterator(); iterator.hasNext(); ) {
                    Location location = iterator.next();
                    Chunk chunk = MinecraftClient.getInstance().world.getChunk(location.getPosition());
                    if (chunk instanceof EmptyChunk || chunk.getBlockState(location.getPosition()).getBlock().hasBlockEntity())
                        continue;
                    iterator.remove();
                }
            }
        }

        public void verify() {
            verifyItems(this);
        }

        private static final Comparator<ItemStack> sorter = Comparator.comparingInt(ItemStack::getCount)
            .reversed()
            .thenComparing(itemStack -> itemStack.getName().getString());

        public List<ItemStack> getItems() {
            Map<Item, Integer> result = new HashMap<>();
            this.forEach(location -> location.getItems()
                .forEach(itemStack -> result.merge(itemStack.getItem(), itemStack.getCount(), Integer::sum))
            );
            return result.keySet().stream()
                .map(item -> new ItemStack(item, result.get(item)))
                .sorted(sorter)
                .collect(Collectors.toList());
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
