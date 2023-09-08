package red.jackf.chesttracker.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
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
                        ModCodecs.ofEnum(MemoryLifetime.class).fieldOf("memoryLifetime")
                                .forGetter(settings -> settings.memoryLifetime),
                        Codec.BOOL.fieldOf("preserveNamed")
                                .forGetter(settings -> settings.preserveNamed)
                ).apply(instance, IntegritySettings::new));

        public boolean removeOnPlayerBlockBreak = true;
        public boolean checkPeriodicallyForMissingBlocks = true;
        public MemoryLifetime memoryLifetime = MemoryLifetime.TWELVE_HOURS;
        public boolean preserveNamed = true;

        public IntegritySettings() {}

        public IntegritySettings(boolean removeOnPlayerBlockBreak,
                                 boolean checkPeriodicallyForMissingBlocks,
                                 MemoryLifetime memoryLifetime,
                                 boolean preserveNamed) {
            this();
            this.removeOnPlayerBlockBreak = removeOnPlayerBlockBreak;
            this.checkPeriodicallyForMissingBlocks = checkPeriodicallyForMissingBlocks;
            this.memoryLifetime = memoryLifetime;
            this.preserveNamed = preserveNamed;
        }

        public enum MemoryLifetime {
            NEVER(null, Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.never")),
            TEN_SECONDS(10L, Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.seconds", 10)),
            FIVE_MINUTES(60L * 5L, Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.minutes", 5)),
            TWENTY_MINUTES(60L * 15L, Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.minutes", 20)),
            FORTY_MINUTES(60L * 30L, Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.minutes", 40)),
            ONE_HOUR(60L * 60L, Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.hour")),
            TWO_HOURS(60L * 60L * 2L, Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.hours", 2)),
            FOUR_HOURS(60L * 60L * 4L, Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.hours", 4)),
            SIX_HOURS(60L * 60L * 6L, Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.hours", 6)),
            TWELVE_HOURS(60L * 60L * 12L, Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.hours", 12)),
            ONE_DAY(60L * 60L * 24L, Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.day")),
            TWO_DAYS(60L * 60L * 24L * 2L, Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.days", 2)),
            FIVE_DAYS(60L * 60L * 24L * 5L, Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.days", 5)),
            SEVEN_DAYS(60L * 60L * 24L * 7L, Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.days", 7));

            public final Long seconds;
            public final Component label;

            MemoryLifetime(@Nullable Long seconds, Component label) {
                this.seconds = seconds;
                this.label = label;
            }
        }
    }
}
