package red.jackf.chesttracker.compat;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.base.internal_api.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.CursedChestType;

import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("UnstableApiUsage")
public abstract class ExpandedStorageHandler {
    public static Collection<BlockPos> check(BlockState state, World world, BlockPos pos) {
        if (state.getBlock() instanceof AbstractChestBlock) {
            if (state.get(AbstractChestBlock.CURSED_CHEST_TYPE) == CursedChestType.SINGLE) return Collections.emptyList();
            else return Collections.singleton(pos.offset(AbstractChestBlock.getDirectionToAttached(state)));
        } else {
            return Collections.emptyList();
        }
    }
}
