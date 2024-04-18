/**
 * <p>Classes for accessing and working with the default provider.</p>
 *
 * <p>The default provider is loaded in Singleplayer, LAN worlds, Realms servers, and a large majority of Multiplayer
 * servers. If providing mod compatibility, it makes more sense to add it to the default provider if possible instead
 * of creating a custom provider - custom providers can still make use of the default.</p>
 *
 * <p>The default provider functions when the GUI is closed; it counts all non-empty item stacks from the GUI that aren't
 * part of the player's inventory and saves them based on position and container. Special handling like the Ender Chest
 * work the same, except they use a different key in the result. This is done in
 * {@link red.jackf.chesttracker.api.providers.defaults.DefaultProviderScreenClose}.</p>
 *
 * <p>Often times, memories get queried from just a level and position. This may require an override in order to provide
 * a more accurate result such as an Ender Chest - this is handled in
 * {@link red.jackf.chesttracker.api.providers.defaults.DefaultProviderMemoryLocation}.</p>
 *
 * <p>The icons for Chest Tracker's sidebar are obtained from the default provider in
 * {@link red.jackf.chesttracker.api.providers.defaults.DefaultProvider#getMemoryKeyIcons()}. If you want to add to the
 * default list (i.e. creating or adding support for a dimension, you can do so in
 * {@link red.jackf.chesttracker.api.providers.defaults.DefaultIcons}.</p>
 *
 * @see red.jackf.chesttracker.api.providers.defaults.DefaultProvider#INSTANCE
 */
package red.jackf.chesttracker.api.providers.defaults;