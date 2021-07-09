package red.jackf.chesttracker.memory;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.GsonHandler;
import red.jackf.chesttracker.mixins.AccessorMinecraftServer;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Environment(EnvType.CLIENT)
public class MemoryDatabase {
    private static final NbtCompound FULL_DURABILITY_TAG = new NbtCompound();
    @Nullable
    private static MemoryDatabase currentDatabase = null;

    static {
        FULL_DURABILITY_TAG.putInt("Damage", 0);
    }

    private transient final String id;
    private ConcurrentMap<Identifier, ConcurrentMap<BlockPos, Memory>> locations = new ConcurrentHashMap<>();
    private transient ConcurrentMap<Identifier, ConcurrentMap<BlockPos, Memory>> namedLocations = new ConcurrentHashMap<>();

    private MemoryDatabase(String id) {
        this.id = id;
    }

    public static void clearCurrent() {
        if (currentDatabase != null) {
            currentDatabase.save();
            currentDatabase = null;
        }
    }

    @Nullable
    public static MemoryDatabase getCurrent() {
        String id = getUsableId();
        if (id == null) return null;
        if (currentDatabase != null && currentDatabase.getId().equals(id)) return currentDatabase;
        MemoryDatabase database = new MemoryDatabase(id);
        database.load();
        currentDatabase = database;
        ChestTracker.LOGGER.info("Loaded " + id);
        return database;
    }

    @Nullable
    private static String getUsableId() {
        MinecraftClient mc = MinecraftClient.getInstance();
        String id = null;
        ClientPlayNetworkHandler cpnh = mc.getNetworkHandler();
        if (cpnh != null && cpnh.getConnection() != null && cpnh.getConnection().isOpen()) {
            if (mc.getServer() != null) {
                id = "singleplayer-" + MemoryUtils.getSingleplayerName(((AccessorMinecraftServer) mc.getServer()).getSession());
            } else if (mc.isConnectedToRealms()) {
                RealmsServer server = MemoryUtils.getLastRealmsServer();
                if (server == null) return null;
                id = "realms-" + MemoryUtils.makeFileSafe(server.owner + "-" + server.getName());
            } else if (mc.getServer() == null && mc.getCurrentServerEntry() != null) {
                id = (mc.getCurrentServerEntry().isLocal() ? "lan-" : "multiplayer-") + MemoryUtils.makeFileSafe(mc.getCurrentServerEntry().address);
            }
        }

        return id;
    }

    public Set<Identifier> getDimensions() {
        return locations.keySet();
    }

    public String getId() {
        return id;
    }

    public void save() {
        Path savePath = getFilePath();
        try {
            try {
                Files.createDirectory(savePath.getParent());
            } catch (FileAlreadyExistsException ignored) {
            }
            FileWriter writer = new FileWriter(savePath.toString());
            GsonHandler.get().toJson(locations, writer);
            writer.flush();
            writer.close();
            ChestTracker.LOGGER.info("Saved data for " + id);
        } catch (Exception ex) {
            ChestTracker.LOGGER.error("Error saving file for " + this.id);
            ChestTracker.LOGGER.error(ex);
        }
    }

    public void load() {
        Path loadPath = getFilePath();
        try {
            if (Files.exists(loadPath)) {
                ChestTracker.LOGGER.info("Found data for " + id);
                FileReader reader = new FileReader(loadPath.toString());

                Map<Identifier, Map<BlockPos, Memory>> raw = GsonHandler.get().fromJson(new JsonReader(reader), new TypeToken<Map<Identifier, Map<BlockPos, Memory>>>() {
                }.getType());
                if (raw == null) {
                    ChestTracker.LOGGER.info("Empty file found for " + id);
                    this.locations = new ConcurrentHashMap<>();
                    this.namedLocations = new ConcurrentHashMap<>();
                } else {
                    // Converts GSON-generated LinkedHashMaps to ConcurrentHashMaps
                    this.locations = new ConcurrentHashMap<>();
                    for (Map.Entry<Identifier, Map<BlockPos, Memory>> entry : raw.entrySet()) {
                        this.locations.put(entry.getKey(), new ConcurrentHashMap<>(entry.getValue()));
                    }
                    this.generateNamedLocations();
                }
            } else {
                ChestTracker.LOGGER.info("No data found for " + id);
                this.locations = new ConcurrentHashMap<>();
                this.namedLocations = new ConcurrentHashMap<>();
            }
        } catch (Exception ex) {
            ChestTracker.LOGGER.error("Error reading file for " + this.id);
            ChestTracker.LOGGER.error(ex);
        }
    }

    // Creates namedLocations list from current locations list.
    private void generateNamedLocations() {
        ConcurrentMap<Identifier, ConcurrentMap<BlockPos, Memory>> namedLocations = new ConcurrentHashMap<>();
        for (Identifier worldId : this.locations.keySet()) {
            ConcurrentMap<BlockPos, Memory> newMap = namedLocations.computeIfAbsent(worldId, id -> new ConcurrentHashMap<>());
            this.locations.get(worldId).forEach(((pos, memory) -> {
                if (memory.getTitle() != null) newMap.put(pos, memory);
            }));
        }
        this.namedLocations = namedLocations;
    }

    @NotNull
    public Path getFilePath() {
        return FabricLoader.getInstance().getGameDir().resolve("chesttracker").resolve(id + ".json");
    }

    public boolean positionExists(Identifier worldId, BlockPos pos) {
        return locations.containsKey(worldId) && locations.get(worldId).containsKey(pos);
    }

    public List<ItemStack> getItems(Identifier worldId) {
        if (locations.containsKey(worldId)) {
            Map<LightweightStack, Integer> count = new HashMap<>();
            Map<BlockPos, Memory> location = locations.get(worldId);
            location.forEach((pos, memory) -> memory.getItems().forEach(stack -> {
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

    public Collection<Memory> getAllMemories(Identifier worldId) {
        if (locations.containsKey(worldId)) {
            return locations.get(worldId).values();
        } else {
            return Collections.emptyList();
        }
    }

    public Collection<Memory> getNamedMemories(Identifier worldId) {
        if (namedLocations.containsKey(worldId)) {
            return namedLocations.get(worldId).values();
        } else {
            return Collections.emptyList();
        }
    }

    public void mergeItems(Identifier worldId, Memory memory, Collection<BlockPos> toRemove) {
        if (!ChestTracker.CONFIG.miscOptions.rememberNewChests && !MemoryUtils.shouldForceNextMerge()) {
            if (locations.containsKey(worldId)) { // check if it's already remembered
                boolean exists = false;
                for (Memory existingMemory : locations.get(worldId).values()) {
                    if (Objects.equals(existingMemory.getPosition(), memory.getPosition())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) return;
            } else { // nothing remembered yet, don't start
                return;
            }
        }

        MemoryUtils.setForceNextMerge(false);

        if (locations.containsKey(worldId)) {
            ConcurrentMap<BlockPos, Memory> map = locations.get(worldId);
            map.remove(memory.getPosition());
            toRemove.forEach(map::remove);
        }
        if (namedLocations.containsKey(worldId)) {
            ConcurrentMap<BlockPos, Memory> map = namedLocations.get(worldId);
            map.remove(memory.getPosition());
            toRemove.forEach(map::remove);
        }
        mergeItems(worldId, memory);
    }

    public void mergeItems(Identifier worldId, Memory memory) {
        if (memory.getItems().size() > 0 || memory.getTitle() != null) {
            addItem(worldId, memory, locations);
            if (memory.getTitle() != null) {
                addItem(worldId, memory, namedLocations);
            }
        }
    }

    private void addItem(Identifier worldId, Memory memory, ConcurrentMap<Identifier, ConcurrentMap<BlockPos, Memory>> map) {
        ConcurrentMap<BlockPos, Memory> memoryMap = map.computeIfAbsent(worldId, (identifier -> new ConcurrentHashMap<>()));
        memoryMap.put(memory.getPosition(), memory);
    }

    public void removePos(Identifier worldId, BlockPos pos) {
        Map<BlockPos, Memory> location = locations.get(worldId);
        if (location != null) location.remove(pos);
        Map<BlockPos, Memory> namedLocation = namedLocations.get(worldId);
        if (namedLocation != null) namedLocation.remove(pos);
    }

    public List<Memory> findItems(ItemStack toFind, Identifier worldId) {
        List<Memory> found = new ArrayList<>();
        Map<BlockPos, Memory> location = locations.get(worldId);
        ClientPlayerEntity playerEntity = MinecraftClient.getInstance().player;
        if (location != null && playerEntity != null) {
            for (Map.Entry<BlockPos, Memory> entry : location.entrySet()) {
                if (entry.getKey() != null) {
                    if (entry.getValue().getItems().stream()
                        .anyMatch(candidate -> MemoryUtils.areStacksEquivalent(toFind, candidate, toFind.getTag() == null || toFind.getTag().equals(FULL_DURABILITY_TAG)))) {
                        if (MemoryUtils.checkExistsInWorld(entry.getValue())) {
                            if (entry.getValue().getPosition() == null // within search range
                                || ChestTracker.getSquareSearchRange() == Integer.MAX_VALUE
                                || entry.getValue().getPosition().getSquaredDistance(playerEntity.getBlockPos()) <= ChestTracker.getSquareSearchRange()) {
                                found.add(entry.getValue());
                            }
                        } else {
                            // Remove if it's disappeared.
                            MemoryDatabase database = MemoryDatabase.getCurrent();
                            if (database != null)
                                database.removePos(worldId, entry.getKey());
                        }
                    }
                }
            }
        }
        return found;
    }

    public void clearDimension(Identifier currentWorldId) {
        locations.remove(currentWorldId);
        namedLocations.remove(currentWorldId);
    }
}
