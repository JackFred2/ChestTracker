package red.jackf.chesttracker.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents a position in a client world. BlockState and BlockEntity is cached on first request.
 */
public interface ClientBlockSource {
    /**
     * X coordinate of the block.
     * @return X coordinate of the block.
     */
    int x();

    /**
     * Y coordinate of the block.
     * @return Y coordinate of the block.
     */
    int y();

    /**
     * Z coordinate of the block.
     * @return Z coordinate of the block.
     */
    int z();

    /**
     * Coordinates of the block position.
     * @return Coordinates of the block position.
     */
    BlockPos pos();

    /**
     * BlockState at the given position; queried after first request then cached.
     * @return BlockState at the given position.
     */
    BlockState blockState();

    /**
     * BlockEntity at the given position; queried after first request then cached. Note that this is the client's view
     * of the BlockEntity, you most likely won't get useful data from it.
     * @return BlockEntity at the given position;
     */
    BlockEntity blockEntity();

    /**
     * Level that the position is in.
     * @return Level that the position is in.
     */
    Level level();
}
