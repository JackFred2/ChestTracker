package red.jackf.chesttracker.storage.backend;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.memory.Memory;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.memory.metadata.Metadata;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.FileUtil;
import red.jackf.chesttracker.util.Timer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JsonBackend extends FileBasedBackend {
    private static final Logger LOGGER = LogManager.getLogger(ChestTracker.class.getCanonicalName() + "/JSON");

    @Override
    public String extension() {
        return ".json";
    }

    @Nullable
    @Override
    public MemoryBank load(String id) {
        Optional<Metadata> metadata = loadMetadata(id);
        if (metadata.isEmpty()) return null;
        Path dataPath = Constants.STORAGE_DIR.resolve(id + extension());
        var result = Timer.time(() -> {
            if (Files.isRegularFile(dataPath)) {
                try {
                    var str = FileUtils.readFileToString(dataPath.toFile(), StandardCharsets.UTF_8);
                    var json = FileUtil.gson().fromJson(str, JsonElement.class);
                    var loaded = MemoryBank.MEMORIES_CODEC.decode(JsonOps.INSTANCE, json).get();
                    if (loaded.right().isPresent()) {
                        throw new IOException("Invalid Memories JSON: %s".formatted(loaded.right().get()));
                    } else {
                        //noinspection OptionalGetWithoutIsPresent
                        return loaded.left().get().getFirst();
                    }
                } catch (JsonParseException | IOException ex) {
                    LOGGER.error("Error loading %s".formatted(dataPath), ex);
                    FileUtil.tryMove(dataPath, dataPath.resolveSibling(dataPath.getFileName() + ".corrupt"), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return null;
        });
        Map<ResourceLocation, Map<BlockPos, Memory>> data = result.getFirst() == null ? new HashMap<>() : result.getFirst();
        LOGGER.debug("Loaded {} in {}ns", dataPath, result.getSecond());
        return new MemoryBank(metadata.get(), data);
    }

    @Override
    public boolean save(MemoryBank memoryBank) {
        LOGGER.debug("Saving {}", memoryBank.getId());

        memoryBank.getMetadata().updateModified();
        boolean metaSaveSuccess = saveMetadata(memoryBank.getId(), memoryBank.getMetadata());
        if (!metaSaveSuccess) return false;

        Path path = Constants.STORAGE_DIR.resolve(memoryBank.getId() + extension());

        try {
            Files.createDirectories(path.getParent());
            Optional<JsonElement> memoryJson = MemoryBank.MEMORIES_CODEC.encodeStart(JsonOps.INSTANCE, memoryBank.getMemories())
                    .resultOrPartial(Util.prefix("Error encoding memories", LOGGER::error));
            if (memoryJson.isPresent()) {
                FileUtils.write(path.toFile(), FileUtil.gson().toJson(memoryJson.get()), StandardCharsets.UTF_8);
                return true;
            } else {
                LOGGER.error("Unknown error encoding memories");
            }
        } catch (IOException ex) {
            LOGGER.error("Error saving memories", ex);
        }

        return false;
    }
}
