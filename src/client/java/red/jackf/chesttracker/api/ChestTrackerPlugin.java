package red.jackf.chesttracker.api;

/**
 * <p>Represents a plugin for Chest Tracker. Place in your <code>fabric.mod.json</code> entrypoints under
 * <code>chesttracker</code>.</p>
 */
public interface ChestTrackerPlugin {

    /**
     * Called when the plugin is loaded, during Chest Tracker's ClientModInitializer.
     *
     * @see red.jackf.chesttracker.api
     */
    void load();
}
