package red.jackf.chesttracker.storage;

import red.jackf.chesttracker.config.ChestTrackerConfig;

public class StorageUtil {
    private StorageUtil() {}

    private static Storage instance;

    static {
        ChestTrackerConfig.INSTANCE.getConfig().memory.storageBackend.load();
    }

    public static Storage getStorage() {
        return instance;
    }

    static void setStorage(Storage storage) {
        instance = storage;
    }
}
