/**
 * <p>Classes pertaining to creating and working with Chest Tracker's providers.</p>
 *
 * <p>Providers control which keys do or can exist, how memories are created, and how the icons are displayed in-game.
 * Most of the time the default provider is loaded, which is the standard behavior of grabbing items from the screen and
 * saving them based on their world dimension key and position. This is also where the special handling for most blocks
 * such as ender chests is performed. The classes for working with and adding to the default provider are located in
 * {@link red.jackf.chesttracker.api.providers.defaults}.</p>
 *
 * <p>As Chest Tracker is only client-sided, it doesn't have an intrinsic sense of where it is at the moment - a level
 * with the Overworld dimension type looks indistinguishable from another one, which poses a problem for Multiworld type
 * servers with hubs.</p>
 *
 * <p>To this end, for some servers it may make sense to use a custom provider. For example, the Hypixel provider has
 * custom handling for Skyblock: it only saves memories on the home island, and has special ender chest handling which
 * filters out the item UI elements. Custom providers can also make use of the default providers as a fallback; this
 * is used for Hypixel SMP servers.</p>
 *
 * <p>To get started with creating a custom provider, see {@link red.jackf.chesttracker.api.providers.ServerProvider}</p>
 */
package red.jackf.chesttracker.api.providers;