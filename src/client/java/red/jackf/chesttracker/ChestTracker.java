package red.jackf.chesttracker;

import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChestTracker implements ClientModInitializer {
    private static final String ID = "chesttracker";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Override
    public void onInitializeClient() {
        LOGGER.debug("Loading ChestTracker");
    }
}
