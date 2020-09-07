package red.jackf.chesttracker.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.chesttracker.memory.MemoryDatabase;

@Environment(EnvType.CLIENT)
@Mixin(Block.class)
public class MixinBlock {

    @Inject(method = "onBreak", at = @At("TAIL"))
    private void chestTracker$handleBlockBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
        MemoryDatabase database = MemoryDatabase.getCurrent();
        if (database != null) database.removePos(world.getRegistryKey().getValue(), pos);
    }
}
