package red.jackf.chesttracker.mixins;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Used to get the folder name of a world for matching JSON file names
 */
@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
    @Accessor("storageSource")
    LevelStorageSource.LevelStorageAccess getStorageSource();
}
