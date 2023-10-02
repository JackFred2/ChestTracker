package red.jackf.chesttracker.api.provider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.api.gui.MemoryKeyIcon;
import red.jackf.chesttracker.gui.GuiConstants;
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
     * <p>Use {@link MemoryBuilder#create(List)} to create an entry and populate it with the given details.</p>
     *
     * @see MemoryBuilder#create(List)
     * @see InteractionTracker#INSTANCE
     * @see ProviderUtils
     * @param screen Screen to create a memory from/
     * @return An optional containing a memory entry, or an empty optional if not present.
     */
    Optional<MemoryBuilder.Entry> createMemory(AbstractContainerScreen<?> screen);

    default List<MemoryKeyIcon> getDefaultIcons() {
        return GuiConstants.DEFAULT_ICONS;
    }

    /**
     * Get the Memory Key that the player is currently in. By default, this is the key representing current level's
     * dimension (minecraft:overworld / minecraft:the_nether / minecraft_the_end). Used to determine which keys to look
     * in for highlights, and for integrity checking.
     *
     * @return An optional containing the player's current location, or an empty optional if not applicable (out of game).
     */
    default Optional<ResourceLocation> getPlayersCurrentKey() {
        var level = Minecraft.getInstance().level;
        if (level == null) return Optional.empty();
        return Optional.of(level.dimension().location());
    }

    static void register(Provider provider) {
        ProviderHandler.register(provider);
    }
}
