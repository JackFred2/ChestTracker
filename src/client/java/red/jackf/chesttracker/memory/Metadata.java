package red.jackf.chesttracker.memory;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.util.ModCodecs;

import java.time.Instant;
import java.util.Optional;

public class Metadata {
    public static final Codec<Metadata> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("name").forGetter(meta -> Optional.ofNullable(meta.name)),
                    ModCodecs.INSTANT.fieldOf("lastModified").forGetter(meta -> meta.lastModified),
                    IntegritySettings.CODEC.fieldOf("integrity").forGetter(meta -> meta.integritySettings)
            ).apply(instance, (name, modified, integrity) -> new Metadata(name.orElse(null), modified, integrity))
    );

    @Nullable
    private String name;
    private Instant lastModified;
    private final IntegritySettings integritySettings;

    public Metadata(@Nullable String name, Instant lastModified, IntegritySettings integritySettings) {
        this.name = name;
        this.lastModified = lastModified;
        this.integritySettings = integritySettings;
    }

    public static Metadata blank() {
        return new Metadata(null, Instant.now(), new IntegritySettings());
    }

    public static Metadata from(String name) {
        return new Metadata(name, Instant.now(), new IntegritySettings());
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public void updateModified() {
        this.lastModified = Instant.now();
    }

    public IntegritySettings getIntegritySettings() {
        return integritySettings;
    }

    public static class IntegritySettings {
        private static final Codec<IntegritySettings> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.BOOL.fieldOf("removeOnPlayerBlockBreak")
                                .forGetter(settings -> settings.removeOnPlayerBlockBreak),
                        Codec.BOOL.fieldOf("checkPeriodicallyForMissingBlocks")
                                .forGetter(settings -> settings.checkPeriodicallyForMissingBlocks),
                        Codec.either(ExtraCodecs.POSITIVE_INT, Codec.unit("never")).fieldOf("memoryExpiryTimeSeconds")
                                .forGetter(settings -> settings.memoryExpiryTimeSeconds == null ? Either.right("never") : Either.left(settings.memoryExpiryTimeSeconds)),
                        Codec.BOOL.fieldOf("alwaysRememberNamed")
                                .forGetter(settings -> settings.alwaysRememberNamed)
                ).apply(instance, IntegritySettings::new));

        public boolean removeOnPlayerBlockBreak = true;
        public boolean checkPeriodicallyForMissingBlocks = true;
        public @Nullable Integer memoryExpiryTimeSeconds = 60 * 60 * 12; // 12 IRL hours
        public boolean alwaysRememberNamed = true;

        public IntegritySettings() {}

        public IntegritySettings(boolean removeOnPlayerBlockBreak,
                                 boolean checkPeriodicallyForMissingBlocks,
                                 Either<Integer, String> memoryExpiryTimeSeconds,
                                 boolean alwaysRememberNamed) {
            this();
            this.removeOnPlayerBlockBreak = removeOnPlayerBlockBreak;
            this.checkPeriodicallyForMissingBlocks = checkPeriodicallyForMissingBlocks;
            this.memoryExpiryTimeSeconds = memoryExpiryTimeSeconds.map(Integer::intValue, s -> null);
            this.alwaysRememberNamed = alwaysRememberNamed;
        }
    }
}
