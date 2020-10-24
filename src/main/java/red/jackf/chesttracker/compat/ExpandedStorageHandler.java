package red.jackf.chesttracker.compat;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.common.block.ChestBlock;
import ninjaphenix.expandedstorage.common.misc.CursedChestType;

import java.util.Collection;
import java.util.Collections;

public abstract class ExpandedStorageHandler {
    public static Collection<BlockPos> check(BlockState state, World world, BlockPos pos) {
        if (state.getBlock() instanceof ChestBlock) {
            if (state.get(ChestBlock.TYPE) == CursedChestType.SINGLE) return Collections.emptyList();
            else return Collections.singleton(pos.offset(ChestBlock.getDirectionToAttached(state)));
        } else {
            return Collections.emptyList();
        }
    }
}
