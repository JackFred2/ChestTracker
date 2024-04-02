package red.jackf.chesttracker.mixins;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.chesttracker.events.AfterPlayerPlaceBlock;

@Mixin(BlockItem.class)
public class BlockItemMixin {


    /**
     * @author JackFred
     * @reason Used to automatically register memories when placed (such as shulker boxes).
     */
    @Inject(method = "place",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;setPlacedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V",
                    shift = At.Shift.AFTER),
            require = 0)
    private void afterBlockPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level instanceof ClientLevel clientLevel && player instanceof LocalPlayer) {
            ItemStack placementStack = context.getItemInHand();
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);

            AfterPlayerPlaceBlock.EVENT.invoker().afterPlayerPlaceBlock(
                clientLevel,
                pos,
                state,
                placementStack
            );
        }
    }
}
