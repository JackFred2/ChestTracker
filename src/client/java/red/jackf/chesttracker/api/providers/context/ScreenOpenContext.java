package red.jackf.chesttracker.api.providers.context;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.providers.MemoryLocation;

/**
 * <p>Context for when a screen is opened in {@link red.jackf.chesttracker.api.providers.ServerProvider#onScreenOpen(ScreenOpenContext)}.</p>
 *
 * <p>It is important that a provider supplies a {@link MemoryLocation} if possible, as this allows Chest Tracker users
 * to use manual mode and client-side names for GUIs.</p>
 */
public interface ScreenOpenContext {
    /**
     * The screen that has been opened. This is called during {@link net.fabricmc.fabric.api.client.screen.v1.ScreenEvents#AFTER_INIT}.
     *
     * @return The screen that has been opened.
     */
    AbstractContainerScreen<?> getScreen();

    /**
     * <p>Sets the location corresponding to a memory for this GUI. You likely want to use the Interaction Tracker in order
     * to get the last interacted block, though this may also be from a command or whatever state you track.</p>
     *
     * <p>If this method is not called during the provider's onScreenOpen event, then client-side renaming and manual
     * mode features will not be shown for the GUI.</p>
     *
     * @see red.jackf.chesttracker.api.providers.InteractionTracker
     * @see red.jackf.chesttracker.api.providers.ServerProvider#getMemoryLocation(ClientBlockSource)
     * @param memoryLocation Memory location that corresponds to this GUI.
     */
    void setMemoryLocation(MemoryLocation memoryLocation);
}
