package red.jackf.chesttracker.compat;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.api.ExpandedStorageAccessors;
import ninjaphenix.expandedstorage.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.block.misc.CursedChestType;

import java.util.Collection;
import java.util.Collections;

public abstract class ExpandedStorageHandler {
    public static Collection<BlockPos> check(BlockState state, World world, BlockPos pos) {
        var attachedDirection = ExpandedStorageAccessors.getAttachedChestDirection(state);
        if (attachedDirection.isPresent()) {
            return Collections.singleton(pos.offset(attachedDirection.get()));
        } else {
            return Collections.emptyList();
        }
    }
}
