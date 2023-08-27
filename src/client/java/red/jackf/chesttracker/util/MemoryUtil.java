package red.jackf.chesttracker.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

public class MemoryUtil {
    public static Vec3 getAverageNameOffset(BlockPos main, Collection<BlockPos> others) {
        var accum = new Vec3(0, 1, 0);
        if (others.isEmpty()) return accum;
        var scale = 1f / (1 + others.size());
        for (BlockPos other : others)
            accum = accum.add(Vec3.atLowerCornerOf(other.subtract(main)).scale(scale));
        return accum;
    }
}
