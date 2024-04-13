package red.jackf.chesttracker.mixins;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.chesttracker.impl.events.AfterPlayerDestroyBlock;
import red.jackf.chesttracker.impl.util.CachedClientBlockSource;

@Mixin(Block.class)
public class BlockMixin {
    /**
     * @author JackFred
     * @reason Used to track when a player destroys a container to keep memories up to date
     */
    @Inject(method = "destroy",
            at = @At("TAIL"),
            require = 0)
    private void afterPlayerDestroy(LevelAccessor level, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (level instanceof ClientLevel clientLevel)
            AfterPlayerDestroyBlock.EVENT.invoker().afterPlayerDestroyBlock(new CachedClientBlockSource(
                clientLevel,
                pos,
                state
            ));
    }
}
