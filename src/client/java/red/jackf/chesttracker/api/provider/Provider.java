package red.jackf.chesttracker.api.provider;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.provider.ProviderHandler;
import red.jackf.jackfredlib.client.api.gps.Coordinate;

import java.util.List;
import java.util.Optional;

/**
 * Providers allow more control over how memories are organized, by changing how memories are recorded. This allows for
 * better compatibility with multiverse-type servers.
 */
public interface Provider {
    /**
     * Identifier for this provider. Displayed to the player in the Memory Bank settings menu.
     *
     * @return Identifier for this provider.
     */
    ResourceLocation name();

    /**
     * Test whether this provider should apply for the given coordinate. Use this to check whether the player is on a
     * given server.
     *
     * @param coordinate Coordinate to test against.
     * @return Whether this provider should be loaded on the given coordinate.
     */
    boolean applies(Coordinate coordinate);

    /**
     * <p>Create a new {@link MemoryBuilder.Entry} from a given screen.</p>
     * <p>Use {@link InteractionTracker#INSTANCE}</p>
     *
     * @see MemoryBuilder#create(List)
     * @param screen Screen to create a memory from/
     * @return An optional containing a memory entry, or an empty optional if not present.
     */
    Optional<MemoryBuilder.Entry> createMemory(AbstractContainerScreen<?> screen);

    static void register(Provider provider) {
        ProviderHandler.register(provider);
    }
}
