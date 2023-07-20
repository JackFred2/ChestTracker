package red.jackf.chesttracker.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.OptionGroup;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.StringUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static net.minecraft.network.chat.Component.translatable;

public class JsonStorage implements Storage {
    private static final Logger LOGGER = LogManager.getLogger(ChestTracker.class.getCanonicalName() + "/JSON");
    private static final Gson GSON_COMPACT = new GsonBuilder().create();
    private static final Gson GSON = GSON_COMPACT.newBuilder().setPrettyPrinting().create();
    private static final String EXT = ".json";

    private static Gson gson() {
        return ChestTrackerConfig.INSTANCE.getConfig().memory.readableJsonMemories ? GSON : GSON_COMPACT;
    }

    @Override
    public MemoryBank load(String id) {
        var path = Constants.STORAGE_DIR.resolve(id + EXT);
        LOGGER.debug("Loading {}", path);
        if (Files.isRegularFile(path)) {
            try {
                var str = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
                var json = gson().fromJson(str, JsonElement.class);
                var loaded = MemoryBank.CODEC.decode(JsonOps.INSTANCE, json).get();
                if (loaded.right().isPresent()) {
                    throw new IOException("Invalid memoryBank JSON: %s".formatted(loaded.right().get()));
                } else {
                    //noinspection OptionalGetWithoutIsPresent
                    return loaded.left().get().getFirst();
                }
            } catch (IOException ex) {
                LOGGER.error("Error loading %s".formatted(path), ex);
            }
        }
        return new MemoryBank();
    }

    @Override
    public void delete(String id) {
        var path = Constants.STORAGE_DIR.resolve(id + EXT);
        if (Files.isRegularFile(path)) {
            try {
                Files.delete(path);
                LOGGER.info("Deleted {}", path);
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
    }

    @Override
    public void save(MemoryBank memoryBank) {
        LOGGER.debug("Saving {}", memoryBank.getId());
        try {
            Files.createDirectories(Constants.STORAGE_DIR);
            var path = Constants.STORAGE_DIR.resolve(memoryBank.getId() + EXT);
            var jsonParsed = MemoryBank.CODEC.encodeStart(JsonOps.INSTANCE, memoryBank).get();
            if (jsonParsed.right().isPresent()) {
                throw new IOException("Error encoding memoryBank to JSON: %s".formatted(jsonParsed.right().get()));
            } else {
                //noinspection OptionalGetWithoutIsPresent
                FileUtils.writeStringToFile(path.toFile(), gson().toJson(jsonParsed.left().get()), StandardCharsets.UTF_8);
            }
        } catch(IOException ex) {
            LOGGER.error("Error saving memories", ex);
        }
    }

    @Override
    public void appendOptionsToSettings(MemoryBank memoryBank, OptionGroup.Builder builder) {
        var path = Constants.STORAGE_DIR.resolve(memoryBank.getId() + EXT);
        var size = Files.isRegularFile(path) ? FileUtils.sizeOf(path.toFile()) : 0L;
        builder.option(LabelOption.create(translatable("chesttracker.config.memory.local.json.fileSize", StringUtil.magnitudeSpace(size, 2) + "B")));
    }

    @Override
    public Collection<String> getAllIds() {
        try(var stream = Files.walk(Constants.STORAGE_DIR)) {
            return stream.filter(path -> path.getFileName().toString().endsWith(EXT))
                    .map(path -> StringUtil.formatPath(Constants.STORAGE_DIR.relativize(path)))
                    .map(s -> s.substring(0, s.length() - EXT.length()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            ChestTracker.LOGGER.error(e);
            return Collections.emptyList();
        }
    }
}
