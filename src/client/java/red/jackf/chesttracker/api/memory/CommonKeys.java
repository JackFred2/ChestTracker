package red.jackf.chesttracker.api.memory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import red.jackf.chesttracker.impl.ChestTracker;

/**
 * List of memory keys used by the core Chest Tracker implementation
 */
public interface CommonKeys {
    //////////////
    // Built-in //
    //////////////

    // Key used for the built-in ender chest compatibility.
    ResourceLocation ENDER_CHEST_KEY = ChestTracker.id("ender_chest");

    // The dimension keys are gained from {@link Level#dimension()}'s location.
    ResourceLocation OVERWORLD = Level.OVERWORLD.location();
    ResourceLocation THE_NETHER = Level.NETHER.location();
    ResourceLocation THE_END = Level.END.location();

    ///////////////////////
    // Mod Compatibility //
    ///////////////////////

    // Share Ender Chest - https://modrinth.com/mod/share-ender-chest
    ResourceLocation SHARE_ENDER_CHEST = ResourceLocation.fromNamespaceAndPath("shareenderchest", "contents");
}
