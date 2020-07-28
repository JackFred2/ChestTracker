package red.jackf.chesttracker.tracker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Per connection storage (i.e. per single player world, server ip, realm)
@Environment(EnvType.CLIENT)
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

    private static Vec3d centerOf(List<BlockPos> positions) {
        Vec3d result = Vec3d.ZERO;
        for (BlockPos pos : positions) {
            result = result.add(Vec3d.of(pos));
        }
        return result.multiply(1d / positions.size());
    }

    private static boolean stacksEqual(ItemStack candidate, ItemStack toFind, boolean matchNbt) {
        if (candidate == null) {
            ChestTracker.LOGGER.warn("Candidate was null!");
            return false;
        }
        if (toFind == null) {
            ChestTracker.LOGGER.warn("ToFind was null!");
            return false;
        }
        if (matchNbt && toFind.hasTag()) {
            return candidate.getItem() == toFind.getItem()
                && Objects.equals(candidate.getTag(), toFind.getTag());
        } else {
            return candidate.getItem() == toFind.getItem();
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

    public Path getFilePath() {
        return ROOT_DIR.resolve(savePath + ".json");
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
                } catch (Throwable e) {
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
        Location location = new Location(pos, title, positions.size() == 1 ? null : offset, items, favourite);

        positions.stream()
            .map(storage.lookupMap::get)
            .filter(Objects::nonNull)
            .forEach(storage::remove);

        storage.add(location);
    }

    public List<Location> findItems(Identifier worldId, ItemStack toFind, boolean matchNbt) {
        WorldStorage storage = getStorage(worldId);
        List<Location> results = storage.stream()
            .filter(location -> location.getItems().stream().anyMatch(itemStack -> stacksEqual(itemStack, toFind, matchNbt)))
            .collect(Collectors.toList());
        storage.verifyItems(results);

        return results;
    }

    public WorldStorage getStorage(Identifier worldId) {
        return this.storage.computeIfAbsent(worldId.toString(), (worldRegistryKey -> new WorldStorage()));
    }

    @Override
    public String toString() {
        return "LocationStorage{" +
            "savePath='" + savePath + '\'' +
            ", storage=" + storage +
            '}';
    }

    // Per world storage
    public static class WorldStorage extends HashSet<Location> {
        private static final Comparator<ItemStack> sorter = Comparator.comparingInt(ItemStack::getCount)
            .reversed()
            .thenComparing(itemStack -> itemStack.getName().getString());
        private final Map<BlockPos, Location> lookupMap = new HashMap<>();

        @Override
        public boolean remove(Object o) {
            removeFromMap((Location) o);
            return super.remove(o);
        }

        private void removeFromMap(Location loc) {
            for (BlockPos pos : LinkedBlocksHandler.getLinked(MinecraftClient.getInstance().world, loc.getPosition()))
                lookupMap.remove(pos);
        }

        @Override
        public void clear() {
            lookupMap.clear();
            super.clear();
        }

        @Nullable
        public Location lookupFast(BlockPos pos) {
            for (BlockPos pos2 : LinkedBlocksHandler.getLinked(MinecraftClient.getInstance().world, pos))
                if (lookupMap.containsKey(pos2)) return lookupMap.get(pos2);
            return null;
        }

        @Override
        public boolean removeIf(@NotNull Predicate<? super Location> filter) {
            boolean removed = false;
            Iterator<Location> iterator = this.iterator();
            while (iterator.hasNext()) {
                Location loc = iterator.next();
                if (filter.test(loc)) {
                    removeFromMap(loc);
                    iterator.remove();
                    removed = true;
                }
            }
            return removed;
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

        public List<ItemStack> getItems() {
            Map<Item, Integer> result = new HashMap<>();
            List<ItemStack> tagged = new ArrayList<>();
            this.forEach(location -> location.getItems().forEach(itemStack -> {
                if (itemStack.hasTag()) {
                    tagged.add(itemStack);
                } else {
                    result.merge(itemStack.getItem(), itemStack.getCount(), Integer::sum);
                }
            }));

            return Stream
                .concat(tagged.stream(),
                    result.keySet().stream().map(item -> new ItemStack(item, result.get(item))))
                .sorted(sorter).collect(Collectors.toList());
        }
    }
}
