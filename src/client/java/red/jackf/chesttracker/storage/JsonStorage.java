package red.jackf.chesttracker.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.apache.commons.io.FileUtils;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.memory.ItemMemory;
import red.jackf.chesttracker.util.Codecs;
import red.jackf.chesttracker.util.Constants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class JsonStorage implements Storage {
    private static final Gson GSON_COMPACT = new GsonBuilder().create();
    private static final Gson GSON = GSON_COMPACT.newBuilder().setPrettyPrinting().create();

    private static Gson gson() {
        return ChestTrackerConfig.INSTANCE.getConfig().memory.readableMemories ? GSON : GSON_COMPACT;
    }

    @Override
    public ItemMemory load(String worldId) {
        var path = Constants.STORAGE_DIR.resolve(worldId + ".json");
        ChestTracker.LOGGER.debug("Loading {}", path);
        if (Files.isRegularFile(path)) {
            try {
                var str = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
                var json = gson().fromJson(str, JsonElement.class);
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
        return new ItemMemory();
    }

    @Override
    public void save(ItemMemory memory) {
        ChestTracker.LOGGER.debug("Saving {}", memory.getId());
        try {
            Files.createDirectories(Constants.STORAGE_DIR);
            var path = Constants.STORAGE_DIR.resolve(memory.getId() + ".json");
            var jsonParsed = Codecs.ITEM_MEMORY.encodeStart(JsonOps.INSTANCE, memory).get();
            if (jsonParsed.right().isPresent()) {
                throw new IOException("Error encoding memory to JSON: %s".formatted(jsonParsed.right().get()));
            } else {
                //noinspection OptionalGetWithoutIsPresent
                FileUtils.writeStringToFile(path.toFile(), gson().toJson(jsonParsed.left().get()), StandardCharsets.UTF_8);
            }
        } catch(IOException ex) {
            ChestTracker.LOGGER.error("Error saving memories", ex);
        }
    }
}
