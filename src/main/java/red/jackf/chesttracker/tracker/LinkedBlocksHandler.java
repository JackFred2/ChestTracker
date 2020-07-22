package red.jackf.chesttracker.tracker;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.minecraft.block.enums.ChestType.LEFT;
import static net.minecraft.block.enums.ChestType.SINGLE;

@Environment(EnvType.CLIENT)
public class LinkedBlocksHandler {

    // Gets all linked positions for multiblocks, including the start.
    public static List<BlockPos> getLinked(World world, BlockPos pos) {
        BlockState root = world.getBlockState(pos);
        if (root.getBlock() instanceof ChestBlock) {
            ChestType type = root.get(ChestBlock.CHEST_TYPE);
            if (type != SINGLE) {
                Direction facing = root.get(ChestBlock.FACING);

                switch (facing) {
                    case NORTH:
                        if (type == LEFT) {
                            return Arrays.asList(pos, pos.add(1, 0, 0));
                        } else {
                            return Arrays.asList(pos, pos.add(-1, 0, 0));
                        }
                    case SOUTH:
                        if (type == LEFT) {
                            return Arrays.asList(pos, pos.add(-1, 0, 0));
                        } else {
                            return Arrays.asList(pos, pos.add(1, 0, 0));
                        }
                    case WEST:
                        if (type == LEFT) {
                            return Arrays.asList(pos, pos.add(0, 0, -1));
                        } else {
                            return Arrays.asList(pos, pos.add(0, 0, 1));
                        }
                    case EAST:
                        if (type == LEFT) {
                            return Arrays.asList(pos, pos.add(0, 0, 1));
                        } else {
                            return Arrays.asList(pos, pos.add(0, 0, -1));
                        }
                }
            }
        }

        return Collections.singletonList(pos);
    }
}
