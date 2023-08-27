package red.jackf.chesttracker.storage.impl;

import net.minecraft.network.chat.Component;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.storage.Storage;
import red.jackf.chesttracker.storage.StorageUtil;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.StringUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

import static net.minecraft.network.chat.Component.translatable;

public interface FileBasedStorage extends Storage {
    Logger LOGGER = LogManager.getLogger(ChestTracker.class.getCanonicalName() + "/File Storage");

    @Override
    default Collection<String> getAllIds() {
        return StorageUtil.getMemoryIdsFilteringFileExtension(extension());
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
