package red.jackf.chesttracker.storage.backend;

import net.minecraft.network.chat.Component;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.StringUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.network.chat.Component.translatable;

public interface FileBasedBackend extends Backend {
    Logger LOGGER = LogManager.getLogger(ChestTracker.class.getCanonicalName() + "/File Storage");

    /**
     * Returns a list of relative file paths for files with a given extension in Chest Tracker's storage location
     *
     * @param extension Extension to filter by e.g. '.json'
     * @return List of strings matching the file extension
     */
    static List<String> getMemoryIdsFilteringFileExtension(String extension) {
        try (var stream = Files.walk(Constants.STORAGE_DIR)) {
            return stream.filter(path -> path.getFileName().toString().endsWith(extension))
                    .map(path -> StringUtil.formatPath(Constants.STORAGE_DIR.relativize(path)))
                    .map(s -> s.substring(0, s.length() - extension.length()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            ChestTracker.LOGGER.error(e);
            return Collections.emptyList();
        }
    }

    @Override
    default Collection<String> getAllIds() {
        return getMemoryIdsFilteringFileExtension(extension());
    }

    @Override
    default void delete(String id) {
        var path = Constants.STORAGE_DIR.resolve(id + extension());
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
    default Component getDescriptionLabel(String memoryBankId) {
        var path = Constants.STORAGE_DIR.resolve(memoryBankId + extension());
        var size = Files.isRegularFile(path) ? FileUtils.sizeOf(path.toFile()) : 0L;
        return translatable("chesttracker.storage.json.fileSize", StringUtil.magnitudeSpace(size, 2) + "B");
    }

    String extension();
}
