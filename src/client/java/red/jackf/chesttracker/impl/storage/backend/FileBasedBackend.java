package red.jackf.chesttracker.impl.storage.backend;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.impl.ChestTracker;
import red.jackf.chesttracker.impl.memory.metadata.Metadata;
import red.jackf.chesttracker.impl.util.Constants;
import red.jackf.chesttracker.impl.util.FileUtil;
import red.jackf.chesttracker.impl.util.Strings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static net.minecraft.network.chat.Component.translatable;

public abstract class FileBasedBackend implements Backend {
    public static final Logger LOGGER = LogManager.getLogger(ChestTracker.class.getCanonicalName() + "/File Storage");

    @Override
    public Collection<String> getAllIds() {
        if (!Files.isDirectory(Constants.STORAGE_DIR)) return Collections.emptyList();
        try (var stream = Files.walk(Constants.STORAGE_DIR)) {
            return stream.filter(path -> path.getFileName().toString().endsWith(metadataExtension()))
                    .map(path -> Strings.formatPath(Constants.STORAGE_DIR.relativize(path)))
                    .map(s -> s.substring(0, s.length() - metadataExtension().length()))
                    .toList();
        } catch (IOException e) {
            LOGGER.error(e);
            return Collections.emptyList();
        }
    }

    @Override
    public void delete(String id) {
        getRelevantPaths(id).forEach(path -> {
            if (Files.isRegularFile(path)) {
                try {
                    Files.delete(path);
                    LOGGER.info("Deleted {}", path);
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        });
    }

    public boolean saveMetadata(String id, Metadata metadata) {
        Path path = Constants.STORAGE_DIR.resolve(id + metadataExtension());
        try {
            Files.createDirectories(path.getParent());
            Optional<JsonElement> metaJson = Metadata.CODEC.encodeStart(JsonOps.INSTANCE, metadata)
                    .resultOrPartial(Util.prefix("Error encoding metadata", LOGGER::error));
            if (metaJson.isPresent()) {
                FileUtils.write(path.toFile(), FileUtil.gson().toJson(metaJson.get()), StandardCharsets.UTF_8);
                return true;
            } else {
                LOGGER.error("Unknown error encoding metadata");
            }
        } catch (IOException e) {
            LOGGER.error("Error saving memories", e);
        }
        return false;
    }

    @Override
    public Optional<Metadata> loadMetadata(String id) {
        Path path = Constants.STORAGE_DIR.resolve(id + metadataExtension());
        if (Files.isRegularFile(path)) {
            try {
                var str = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
                var json = FileUtil.gson().fromJson(str, JsonElement.class);
                AtomicReference<Metadata> metadata = new AtomicReference<>(null);
                Metadata.CODEC.decode(JsonOps.INSTANCE, json)
                        .resultOrPartial(Util.prefix("Invalid metadata JSON: " + id, LOGGER::error))
                        .ifPresent(pair -> metadata.set(pair.getFirst()));
                if (metadata.get() != null) {
                    return Optional.ofNullable(metadata.get());
                }
            } catch (JsonParseException | IOException ex) {
                LOGGER.error("Error decoding metadata", ex);
                FileUtil.tryMove(path, path.resolveSibling(path.getFileName() + ".corrupt"), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return Optional.empty();
    }

    @Override
    public Component getDescriptionLabel(String memoryBankId) {
        long size = getRelevantPaths(memoryBankId).stream().mapToLong(FileBasedBackend::getSizeIfPresent).sum();
        return translatable("chesttracker.storage.json.fileSize", Strings.magnitudeSpace(size, 2) + "B");
    }

    public abstract String extension();

    protected String metadataExtension() {
        return extension() + ".meta";
    }

    protected List<Path> getRelevantPaths(String id) {
        return List.of(
                Constants.STORAGE_DIR.resolve(id + extension()),
                Constants.STORAGE_DIR.resolve(id + metadataExtension())
        );
    }

    private static long getSizeIfPresent(Path path) {
        return Files.isRegularFile(path) ? FileUtils.sizeOf(path.toFile()) : 0L;
    }
}
