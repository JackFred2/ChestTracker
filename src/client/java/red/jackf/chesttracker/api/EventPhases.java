package red.jackf.chesttracker.api;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.ChestTracker;

public class EventPhases {
    /**
     * Phase that gets called before all the others. Generally used in case a mod wants to override Chest Tracker's
     * default behavior.
     */
    public static final ResourceLocation PRIORITY_PHASE = ChestTracker.id("priority");

    /**
     * Normal phase priority, should be the normally used phase.
     */
    public static final ResourceLocation DEFAULT_PHASE = Event.DEFAULT_PHASE;

    /**
     * Phase that gets called after all the others. Used by Chest Tracker to provide fallback behavior, such as the
     * default right-click.
     */
    public static final ResourceLocation FALLBACK_PHASE = ChestTracker.id("fallback");

    private EventPhases() {
    }
}
