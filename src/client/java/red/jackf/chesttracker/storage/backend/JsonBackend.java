package red.jackf.chesttracker.storage.backend;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.memory.metadata.Metadata;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.FileUtil;
import red.jackf.chesttracker.util.Timer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class JsonBackend implements FileBasedBackend {
    private static final Logger LOGGER = LogManager.getLogger(ChestTracker.class.getCanonicalName() + "/JSON");
    private static final Gson GSON_COMPACT = new GsonBuilder().create();
    private static final Gson GSON = GSON_COMPACT.newBuilder().setPrettyPrinting().create();
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static Gson gson() {
        return ChestTrackerConfig.INSTANCE.getConfig().storage.readableJsonMemories ? GSON : GSON_COMPACT;
    }

    @Override
    public String extension() {
        return ".json";
    }

    @Nullable
    @Override
    public MemoryBank load(String id) {
        var path = Constants.STORAGE_DIR.resolve(id + extension());
        var result = Timer.time(() -> {
            if (Files.isRegularFile(path)) {
                try {
                    var str = FileUtils.readFileToString(path.toFile(), CHARSET);
                    var json = gson().fromJson(str, JsonElement.class);
                    var loaded = MemoryBank.CODEC.decode(JsonOps.INSTANCE, json).get();
                    if (loaded.right().isPresent()) {
                        throw new IOException("Invalid Memory Bank JSON: %s".formatted(loaded.right().get()));
                    } else {
                        //noinspection OptionalGetWithoutIsPresent
                        return loaded.left().get().getFirst();
                    }
                } catch (JsonParseException | IOException ex) {
                    LOGGER.error("Error loading %s".formatted(path), ex);
                    FileUtil.tryMove(path, path.resolveSibling(path.getFileName() + ".corrupt"), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return null;
        });
        LOGGER.debug("Loaded {} in {}ns", path, result.getSecond());
        return result.getFirst();
    }

    @Override
    public @Nullable Metadata getMetadata(String id) {
        var path = Constants.STORAGE_DIR.resolve(id + extension());
        var result = Timer.time(() -> {
            if (Files.isRegularFile(path)) {
                try (var reader = new JsonReader(new InputStreamReader(new FileInputStream(path.toFile()), CHARSET))) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        if (reader.nextName().equals("metadata")) {
                            var element = JsonParser.parseReader(reader);
                            return tryParseRawMetadata(element);
                        } else {
                            reader.skipValue();
                        }
                    }
                } catch (JsonParseException | IOException e) {
                    LOGGER.error("Error loading metadata for %s".formatted(id), e);
                    FileUtil.tryMove(path, path.resolveSibling(path.getFileName() + ".corrupt"), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return null;
        });
        LOGGER.debug("Loaded metadata for {} in {}ns", path, result.getSecond());
        return result.getFirst();
    }

    @Nullable
    private Metadata tryParseRawMetadata(JsonElement element) {
        return Metadata.CODEC.decode(JsonOps.INSTANCE, element)
                .resultOrPartial(LOGGER::error)
                .map(Pair::getFirst)
                .orElse(null);
    }

    @Override
    public void save(MemoryBank memoryBank) {
        LOGGER.debug("Saving {}", memoryBank.getId());
        memoryBank.getMetadata().updateModified();
        try {
            Files.createDirectories(Constants.STORAGE_DIR);
            var path = Constants.STORAGE_DIR.resolve(memoryBank.getId() + extension());
            var jsonParsed = MemoryBank.CODEC.encodeStart(JsonOps.INSTANCE, memoryBank).get();
            if (jsonParsed.right().isPresent()) {
                throw new IOException("Error encoding memoryBank to JSON: %s".formatted(jsonParsed.right().get()));
            } else {
                //noinspection OptionalGetWithoutIsPresent
                FileUtils.writeStringToFile(path.toFile(), gson().toJson(jsonParsed.left().get()), CHARSET);
            }
        } catch (IOException ex) {
            LOGGER.error("Error saving memories", ex);
        }
    }
}
