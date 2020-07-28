package red.jackf.chesttracker.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.chesttracker.tracker.Location;
import red.jackf.chesttracker.tracker.LocationStorage;

@Mixin(Block.class)
public class MixinBlock {

    @Inject(method = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V",
        at = @At("TAIL"))
    private void trackerHandleBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
        LocationStorage storage = LocationStorage.get();
        if (storage != null) {
            LocationStorage.WorldStorage worldStorage = storage.getStorage(world.getRegistryKey().getValue());
            Location loc = worldStorage != null ? worldStorage.lookupFast(pos) : null;
            if (loc != null) worldStorage.remove(loc);
        }
    }
}
