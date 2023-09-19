package red.jackf.chesttracker.api.location;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents a position in a client world. BlockState and BlockEntity is cached on first request.
 */
public interface ClientBlockSource {
    int x();

    int y();

    int z();

    BlockPos pos();

    BlockState blockState();

    BlockEntity blockEntity();

    ClientLevel level();
}
