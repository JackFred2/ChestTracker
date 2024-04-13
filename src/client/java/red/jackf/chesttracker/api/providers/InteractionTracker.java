package red.jackf.chesttracker.api.providers;

import net.minecraft.client.multiplayer.ClientLevel;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.impl.providers.InteractionTrackerImpl;

import java.util.Optional;

/**
 * Helper class for checking the last interacts of the player.
 */
public interface InteractionTracker {
    InteractionTracker INSTANCE = InteractionTrackerImpl.INSTANCE;

    /**
     * Get the player's current level. Usually present, unless the player is not in-game. Used by the default provider to
     * get the correct Memory Key (<code>level.dimension().location()</code>).
     *
     * @return An optional containing the player's current level
     */
    Optional<ClientLevel> getPlayerLevel();

    /**
     * Get a block source containing details about the last interacted block, if the last interaction <i>was</i> a block.
     *
     * @return An optional containing information about the last interacted block, or an empty optional if not in-game or
     * the last interaction wasn't a block.
     */
    Optional<ClientBlockSource> getLastBlockSource();
}
