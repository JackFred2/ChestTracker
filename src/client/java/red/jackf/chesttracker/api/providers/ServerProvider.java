package red.jackf.chesttracker.api.providers;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.providers.defaults.DefaultIcons;
import red.jackf.jackfredlib.client.api.gps.Coordinate;

import java.util.List;
import java.util.Optional;

/**
 * Server providers allow for custom Chest Tracker behavior when connected to a server. This allows for more specialized handling,
 * such as when or if to load different memory banks, and for more consistent multi-world support.
 */
@ApiStatus.AvailableSince("2.4.0")
public abstract class ServerProvider {
    ///////////
    // SETUP //
    ///////////

    /**
     * <p>When connecting to a server or world, all registered providers are sorted highest to lowest priority, then the first
     * to pass {@link #appliesTo(Coordinate)} will be selected. Providers with identical priorities are chosen arbitrarily.</p>
     *
     * <p>The default provider has a priority of -100, and will be chosen last.</p>
     *
     * @return The priority of this provider.
     */
    @ApiStatus.OverrideOnly
    public int getPriority() {
        return 0;
    }

    /**
     * Whether this provider should apply to a given connection. Commonly used to filter by server IPs:
     * <pre>
     * {@code
     *  return coordinate instanceof Coordinate.Multiplayer multiplayer
     *      && multiplayer.address().contains("hypixel.net");
     * }
     * </pre>
     * @param coordinate The connection coordinate to check against.
     * @return Whether this provider is applicable to the given coordinate.
     */
    @ApiStatus.OverrideOnly
    public abstract boolean appliesTo(Coordinate coordinate);

    /////////////
    // LOADING //
    /////////////

    /**
     * Called when this provider is selected for a given location. May be used to immediately load a given memory bank;
     * or to set up state using methods in {@link red.jackf.jackfredlib.client.api.gps}.
     *
     * @param coordinate Coordinate loaded in with.
     */
    @ApiStatus.OverrideOnly
    public abstract void onConnect(Coordinate coordinate);

    /**
     * Called when a player respawns or changes level. May be used in multi-world servers or servers with hubs such as
     * Hypixel or Origin Realms.
     *
     * @param from Client-side level key that the player is leaving.
     * @param to Client-side level key that the player is connecting to.
     */
    @ApiStatus.OverrideOnly
    public void onRespawn(ResourceKey<Level> from, ResourceKey<Level> to) {}

    /**
     * Called when a player has disconnected from a given connection. May be used to tear down any state;
     * any Memory Bank still loaded will be unloaded after this method.
     */
    @ApiStatus.OverrideOnly
    public void onDisconnect() {}

    ////////////
    // EVENTS //
    ////////////

    /**
     * Called when a GUI with slots is closed. May be used to count items on a screen and submit them to a memory bank.
     *
     * @param context Context for the screen closing. Contains details about the current screen and utilities for handling
     *                items.
     */
    public void onScreenClose(ScreenCloseContext context) {}

    /**
     * Called when the local player places a block. May be used to automatically add memories from a Shulker Box or similar
     * items.
     *
     * @param context Context for the block placement. Contains details about the state and <b>local</b> block entity at
     *                the position, and utilities for getting items from the tag.
     */
    public void onBlockPlaced(BlockPlacedContext context) {}

    /**
     * Called when a player receives a game message (<i>not a player chat message)</i>. Can be used to update state (e.g.
     * Hypixel sends a chat message containing the unique SMP ID).
     *
     * @param message The message as it was received.
     * @param isOverlay Whether this chat message is displaying on the action bar or in the chat window.
     */
    public void onGameMessageReceived(Component message, boolean isOverlay) {}

    /**
     * Called when the local player sends a command to the server. May be used to update state (for example, an <code>/enderchest</code>
     * command.
     *
     * @param command Command text sent; does not contain the leading slash.
     */
    public void onCommandSent(String command) {}

    ///////////
    // OTHER //
    ///////////

    /**
     * A memory key ID and block position corresponding to a given location in the world. This is used for compatibility
     * with mods such as WTHIT or Jade, in order to get an appropriate Memory for blocks with custom handling, such as
     * ender chests.
     *
     * @param cbs Block source being queried. Contains methods for getting the level, block state and position.
     * @return An optional containing a memory key and block position override for a given block, or an empty optional otherwise.
     */
    public Optional<Pair<ResourceLocation, BlockPos>> getMemoryKeyOverride(ClientBlockSource cbs) {
        return Optional.empty();
    }

    /**
     * Should return the local player's current equivalent memory key. This is used for the default tab when opening the
     * main screen, getting names for rendering, for integrity checking, and more.
     *
     * @return An optional containing the local player's current memory key, or an empty optional if not in a key.
     */
    @ApiStatus.OverrideOnly
    public Optional<ResourceLocation> getPlayersCurrentKey(ClientLevel level, LocalPlayer player) {
        return Optional.of(level.dimension().location());
    }

    /**
     * <p>Returns a list of memory key icons used for this provider. These are icons that are shown on the left of the
     * Chest Tracker screen for different keys / dimensions, and will show up in the order of the supply list (unless
     * manually reordered by the user).</p>
     *
     * <p>You may find it useful to append the default icons in some order from {@link DefaultIcons()}.</p>
     *
     * @return A list of memory key icons used by this provider.
     */
    public List<MemoryKeyIcon> getMemoryKeyIcons() {
        return ProviderUtils.getDefaultIcons();
    }
}
