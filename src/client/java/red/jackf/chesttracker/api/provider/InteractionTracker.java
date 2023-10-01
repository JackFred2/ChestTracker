package red.jackf.chesttracker.api.provider;

import net.minecraft.client.multiplayer.ClientLevel;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.provider.InteractionTrackerImpl;

import java.util.Optional;

public interface InteractionTracker {
    InteractionTracker INSTANCE = InteractionTrackerImpl.INSTANCE;

    Optional<ClientLevel> getPlayerLevel();

    Optional<ClientBlockSource> getLastBlockSource();
}
