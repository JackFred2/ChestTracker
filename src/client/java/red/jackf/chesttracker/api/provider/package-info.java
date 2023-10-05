/**
 * Package for creating custom {@link red.jackf.chesttracker.api.provider.Provider}s, which provide better compatibility
 * for multiverse-type servers than using the APIs in the {@link red.jackf.chesttracker.api.provider.def} package.
 * Providers allow custom parsing of screens and memory keys to display to the user. An example implementation, for
 * Hypixel Skyblock, can be found {@link red.jackf.chesttracker.compat.servers.hypixel}.
 */
@ApiStatus.Experimental
package red.jackf.chesttracker.api.provider;

import org.jetbrains.annotations.ApiStatus;