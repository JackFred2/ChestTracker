package red.jackf.chesttracker.impl.compat.mods;

import compasses.expandedstorage.api.ExpandedStorageAccessors;
import compasses.expandedstorage.impl.client.gui.AbstractScreen;
import red.jackf.chesttracker.api.gui.GetCustomName;
import red.jackf.jackfredlib.api.base.ResultHolder;
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

        GetCustomName.EVENT.register(screen -> {
            if (screen instanceof AbstractScreen) {
                return ResultHolder.empty();
            }
            return ResultHolder.pass();
        });
    }
}
