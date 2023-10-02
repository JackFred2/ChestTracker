package red.jackf.chesttracker.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.Function;

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
                    return DataResult.error(() -> "Unknown number of coordinates: " + split.length);
                }
            }, pos -> "%d,%d,%d".formatted(pos.getX(), pos.getY(), pos.getZ())
    );

    public static final Codec<ItemStack> ITEM_STACK_IGNORE_COUNT = ExtraCodecs.xor(
            Codec.pair(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").codec(),
                    CompoundTag.CODEC.fieldOf("tag").codec()
            ),
            BuiltInRegistries.ITEM.byNameCodec()
    ).xmap(ModCodecs::decodeEitherItemStack, stack -> stack.hasTag() ? Either.left(Pair.of(stack.getItem(), stack.getTag())) : Either.right(stack.getItem()));

    private static ItemStack decodeEitherItemStack(Either<Pair<Item, CompoundTag>, Item> either) {
        if (either.left().isPresent()) {
            var pair = either.left().get();
            var stack = new ItemStack(pair.getFirst());
            stack.setTag(pair.getSecond());
            return stack;
        } else {
            return new ItemStack(either.right().orElseThrow());
        }
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Creates a codec for a given enum class.
     *
     * @param enumClass Class of the enum to decode/encode.
     * @return Codec representing the given enum class
     * @param <E> Type of enum being decoded/encoded.
     */
    public static <E extends Enum<E>> Codec<E> ofEnum(Class<E> enumClass) {
        return Codec.STRING.comapFlatMap(s -> {
            try {
                return DataResult.success(Enum.valueOf(enumClass, s));
            } catch (IllegalArgumentException ex) {
                return DataResult.error(() -> "Unknown enum constant for " + enumClass.getSimpleName() + ": " + s);
            }
        }, Enum::toString);
    }

    /**
     * Creates a codec that can only decode into a single value.
     * @param typeCodec Base codec to use
     * @param value Value to allow
     * @return Codec only allowing serialization to a given value
     * @param <T> Type of serialized value
     */
    public static <T> Codec<T> singular(Codec<T> typeCodec, T value) {
        return oneOf(typeCodec, Collections.singleton(value));
    }

    /**
     * Returns a codec for a type, while filtering for certain values during decoding.
     *
     * @param baseCodec Base codec to use
     * @param values Value whitelist to check against when decoding.
     * @return A codec which filters for specific values on decoding.
     * @param <T> Type of value being encoded/decoded
     */
    public static <T> Codec<T> oneOf(Codec<T> baseCodec, Collection<T> values) {
        var finalValues = Set.copyOf(values);
        return baseCodec.comapFlatMap(t -> {
            if (finalValues.contains(t)) {
                return DataResult.success(t);
            } else {
                return DataResult.error(() -> "Unknown element: %s".formatted(t));
            }
        }, Function.identity());
    }

    /**
     * Makes a list codec return a mutable list instance of the default immutable one.
     *
     * @param codec Codec that returns an immutable list
     * @param <T>   Type contained in lists in said codecs
     * @return Codec that provides a mutable list on deserialization ({@link ArrayList})
     */
    public static <T> Codec<List<T>> makeMutableList(Codec<List<T>> codec) {
        return codec.xmap(ArrayList::new, Function.identity());
    }

    /**
     * Makes a map codec return a mutable map instead of the default immutable one.
     *
     * @param codec Codec that returns an immutable map
     * @param <K>   Type of key in said maps
     * @param <V>   Type of value in said maps
     * @return Codec that provides a mutable map on deserialization ({@link HashMap})
     */
    public static <K, V> Codec<Map<K, V>> makeMutableMap(Codec<Map<K, V>> codec) {
        return codec.xmap(HashMap::new, Function.identity());
    }
}
