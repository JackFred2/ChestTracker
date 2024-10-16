package red.jackf.chesttracker.impl.storage.backend;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.impl.ChestTracker;
import red.jackf.chesttracker.impl.memory.MemoryBankImpl;
import red.jackf.chesttracker.impl.memory.MemoryKeyImpl;
import red.jackf.chesttracker.impl.memory.metadata.Metadata;
import red.jackf.chesttracker.impl.util.Constants;
import red.jackf.chesttracker.impl.util.FileUtil;
import red.jackf.chesttracker.impl.util.Misc;

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
    public MemoryBankImpl load(String id, @Nullable HolderLookup.Provider registries) {
        DynamicOps<JsonElement> ops = registries == null ? JsonOps.INSTANCE : registries.createSerializationContext(JsonOps.INSTANCE);

        Optional<Metadata> metadata = loadMetadata(id);
        if (metadata.isEmpty()) return null;
        Path dataPath = Constants.STORAGE_DIR.resolve(id + extension());
        var result = Misc.time(() -> {
            if (Files.isRegularFile(dataPath)) {
                try {
                    var str = FileUtils.readFileToString(dataPath.toFile(), StandardCharsets.UTF_8);
                    var json = FileUtil.gson().fromJson(str, JsonElement.class);
                    var decoded = MemoryBankImpl.DATA_CODEC.decode(ops, json);
                    if (decoded.isError()) {
                        //noinspection OptionalGetWithoutIsPresent
                        throw new IOException("Invalid Memories JSON: %s".formatted(decoded.error().get().message()));
                    } else {
                        //noinspection OptionalGetWithoutIsPresent
                        return decoded.result().get().getFirst();
                    }
                } catch (JsonParseException | IOException ex) {
                    LOGGER.error("Error loading %s".formatted(dataPath), ex);
                    FileUtil.tryMove(dataPath, dataPath.resolveSibling(dataPath.getFileName() + ".corrupt"), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return null;
        });
        Map<ResourceLocation, MemoryKeyImpl> data = result.getFirst() == null ? new HashMap<>() : result.getFirst();
        LOGGER.debug("Loaded {} in {}ns", dataPath, result.getSecond());
        return new MemoryBankImpl(metadata.get(), data);
    }

    @Override
    public boolean save(MemoryBankImpl memoryBank, @Nullable HolderLookup.Provider registries) {
        LOGGER.debug("Saving {}", memoryBank.getId());

        DynamicOps<JsonElement> ops = registries == null ? JsonOps.INSTANCE : registries.createSerializationContext(JsonOps.INSTANCE);

        memoryBank.getMetadata().updateModified();
        boolean metaSaveSuccess = saveMetadata(memoryBank.getId(), memoryBank.getMetadata());
        if (!metaSaveSuccess) return false;

        Path path = Constants.STORAGE_DIR.resolve(memoryBank.getId() + extension());

        try {
            Files.createDirectories(path.getParent());
            Optional<JsonElement> memoryJson = MemoryBankImpl.DATA_CODEC.encodeStart(ops, memoryBank.getMemories())
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
