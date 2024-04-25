package red.jackf.chesttracker.impl.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Codecs for classes that aren't ours
 */
public class ModCodecs {
    /**
     * Short form block pos codec
     */
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
                    return DataResult.error(() -> "Invalid number of coordinates: " + split.length);
                }
            }, pos -> "%d,%d,%d".formatted(pos.getX(), pos.getY(), pos.getZ())
    );

    /**
     * Compact codec for an ItemStack. Ignores the count on both serialization and deserialization
     */
    public static final Codec<ItemStack> ITEM_STACK_IGNORE_COUNT = Codec.xor(
            Codec.pair(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").codec(),
                    DataComponentPatch.CODEC.fieldOf("patch").codec()
            ),
            BuiltInRegistries.ITEM.byNameCodec()
    ).xmap(ModCodecs::decodeEitherItemStack, stack -> !stack.getComponentsPatch().isEmpty() ? Either.left(Pair.of(stack.getItem(), stack.getComponentsPatch())) : Either.right(stack.getItem()));

    private static ItemStack decodeEitherItemStack(Either<Pair<Item, DataComponentPatch>, Item> either) {
        if (either.left().isPresent()) {
            var pair = either.left().get();
            var stack = new ItemStack(pair.getFirst());
            stack.applyComponents(pair.getSecond());
            return stack;
        } else {
            return new ItemStack(either.right().orElseThrow());
        }
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Creates a codec that can only decode into a single value.
     * @param typeCodec Base codec to use
     * @param value Value to allow
     * @return Codec only allowing serialization to a given value
     * @param <T> Type of serialized value
     */
    public static <T> Codec<T> singular(Codec<T> typeCodec, T value) {
        return JFLCodecs.oneOf(typeCodec, Collections.singleton(value));
    }

    public static <T> Codec<Set<T>> set(Codec<T> base) {
        return base.listOf().xmap(Set::copyOf, List::copyOf);
    }
}
