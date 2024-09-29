package red.jackf.chesttracker.impl.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    public static final Codec<ItemStack> OPTIONAL_ITEM_STACK = optionalEmptyMap(ItemStack.CODEC)
            .xmap(optional -> optional.orElse(ItemStack.EMPTY), itemStack -> itemStack.isEmpty() ? Optional.empty() : Optional.of(itemStack));

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



    public static <A> Codec<Optional<A>> optionalEmptyMap(Codec<A> codec) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<Optional<A>, T>> decode(DynamicOps<T> dynamicOps, T object) {
                return isEmptyMap(dynamicOps, object)
                        ? DataResult.success(Pair.of(Optional.empty(), object))
                        : codec.decode(dynamicOps, object).map(pair -> pair.mapFirst(Optional::of));
            }

            private static <T> boolean isEmptyMap(DynamicOps<T> ops, T value) {
                Optional<MapLike<T>> optional = ops.getMap(value).result();
                return optional.isPresent() && (optional.get()).entries().findAny().isEmpty();
            }

            public <T> DataResult<T> encode(Optional<A> input, DynamicOps<T> ops, T value) {
                return input.isEmpty() ? DataResult.success(ops.emptyMap()) : codec.encode(input.get(), ops, value);
            }
        };
    }
}
