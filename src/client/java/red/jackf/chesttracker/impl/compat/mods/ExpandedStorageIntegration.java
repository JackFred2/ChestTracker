package red.jackf.chesttracker.impl.compat.mods;

import compasses.expandedstorage.api.ExpandedStorageAccessors;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;

/**
 * Adds connected block support to expanded storage chests
 */
public class ExpandedStorageIntegration {
    public static void setup() {
        // should probably be in where is it
        ConnectedBlocksGrabber.EVENT.register((positions, pos, level, state) -> {
            ExpandedStorageAccessors.getAttachedChestDirection(state).ifPresent(direction -> {
                positions.add(pos.relative(direction));
            });
        });
    }
}
