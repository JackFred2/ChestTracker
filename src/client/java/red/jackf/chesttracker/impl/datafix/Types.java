package red.jackf.chesttracker.impl.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.util.datafix.fixes.References;

public class Types {
    public static final DSL.TypeReference MEMORY_DATA = References.reference("chesttracker/memory_data");

    private static final String VERSION_KEY = "MinecraftDataVersion";

    public static <T> Codec<T> wrap(DSL.TypeReference type, Codec<T> codec, int fallbackVersion) {
        final DataFixer fixer = Minecraft.getInstance().getFixerUpper();
        final int currentVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();

        return new Codec<>() {
            @Override
            public <A> DataResult<A> encode(T input, DynamicOps<A> ops, A prefix) {
                return codec.encode(input, ops, prefix)
                        .flatMap(encoded -> ops.mergeToMap(encoded, ops.createString(VERSION_KEY), ops.createInt(currentVersion)));
            }

            @Override
            public <A> DataResult<Pair<T, A>> decode(DynamicOps<A> ops, A input) {
                int version = ops.get(input, VERSION_KEY).flatMap(ops::getNumberValue).map(Number::intValue).result().orElse(fallbackVersion);
                Dynamic<A> stripped = new Dynamic<>(ops, ops.remove(input, VERSION_KEY));
                Dynamic<A> fixedUpped = fixer.update(type, stripped, version, currentVersion);
                return codec.decode(fixedUpped);
            }
        };
    }
}
