package red.jackf.chesttracker.compat;

import io.github.cottonmc.component.UniversalComponents;
import io.github.cottonmc.component.api.ComponentHelper;
import io.github.cottonmc.component.compat.core.BlockComponentHook;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public abstract class UniversalComponentsHandler {
    public static boolean isValidInventoryHolder(Block block, World world, BlockPos pos) {
        return block instanceof BlockEntityProvider || ComponentHelper.INVENTORY.hasComponent(world, pos, null, "minecraft");
    }
}
