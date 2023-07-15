package red.jackf.chesttracker.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.memory.ItemMemory;
import red.jackf.chesttracker.util.Codecs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class JsonStorage implements Storage {
    private static final Path STORAGE_DIR = FabricLoader.getInstance().getGameDir().resolve("chesttracker");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public ItemMemory load(String worldId) {
        var path = STORAGE_DIR.resolve(worldId + ".json");
        ChestTracker.LOGGER.debug("Loading {}", path);
        if (Files.isRegularFile(path)) {
            try {
                var str = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
                var json = GSON.fromJson(str, JsonElement.class);
                var loaded = Codecs.ITEM_MEMORY.decode(JsonOps.INSTANCE, json).get();
                if (loaded.right().isPresent()) {
                    throw new IOException("Invalid memory JSON: %s".formatted(loaded.right().get()));
                } else {
                    //noinspection OptionalGetWithoutIsPresent
                    return loaded.left().get().getFirst();
                }
            } catch (IOException ex) {
                ChestTracker.LOGGER.error("Error loading %s".formatted(path), ex);
            }
        }
        return new ItemMemory(new HashMap<>());
    }

    @Override
    public void save(ItemMemory memory) {
        ChestTracker.LOGGER.debug("Saving {}", memory.getId());
        try {
            Files.createDirectories(STORAGE_DIR);
            var path = STORAGE_DIR.resolve(memory.getId() + ".json");
            var jsonParsed = Codecs.ITEM_MEMORY.encodeStart(JsonOps.INSTANCE, memory).get();
            if (jsonParsed.right().isPresent()) {
                throw new IOException("Error encoding memory to JSON: %s".formatted(jsonParsed.right().get()));
            } else {
                //noinspection OptionalGetWithoutIsPresent
                FileUtils.writeStringToFile(path.toFile(), GSON.toJson(jsonParsed.left().get()), StandardCharsets.UTF_8);
            }
        } catch(IOException ex) {
            ChestTracker.LOGGER.error("Error saving memories", ex);
        }
    }
}
