package red.jackf.chesttracker.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.memory.ItemMemory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Codecs {
    public static final Codec<BlockPos> BLOCK_POS_STRING = Codec.STRING.comapFlatMap(
            s -> {
                String[] split = s.split(",");
                if (split.length == 3) {
                    try {
                        int x = Integer.parseInt(split[0]);
                        int y = Integer.parseInt(split[1]);
                        int z = Integer.parseInt(split[2]);

                        return DataResult.success(new BlockPos(x, y, z));
                    } catch (NumberFormatException ex) {
                        return DataResult.error(() -> "Invalid integer in key");
                    }
                } else {
                    return DataResult.error(() -> "Unknown number of coordinates: " + split.length);
                }
            }, pos -> "%d,%d,%d".formatted(pos.getX(), pos.getY(), pos.getZ())
    );
    public static final Codec<ItemMemory> ITEM_MEMORY = RecordCodecBuilder.create(instance ->
            instance.group(Codec.unboundedMap(
                    ResourceLocation.CODEC,
                    Codec.unboundedMap(
                            BLOCK_POS_STRING,
                            ItemStack.CODEC.listOf()
                    ).xmap(map -> (Map<BlockPos, List<ItemStack>>) new HashMap<>(map), Function.identity())
            ).xmap(map -> (Map<ResourceLocation, Map<BlockPos, List<ItemStack>>>) new HashMap<>(map), Function.identity())
                    .fieldOf("memories").forGetter(ItemMemory::getMemories)).apply(instance, ItemMemory::new
            ));
}
