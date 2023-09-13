package red.jackf.chesttracker.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.gui.MemoryKeyIcon;
import red.jackf.chesttracker.util.ModCodecs;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Metadata {
    public static final Codec<Metadata> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("name").forGetter(meta -> Optional.ofNullable(meta.name)),
                    ModCodecs.INSTANT.optionalFieldOf("lastModified").forGetter(meta -> Optional.of(meta.lastModified)),
                    MemoryKeyIcon.CODEC.listOf().optionalFieldOf("icons").forGetter(meta -> Optional.of(meta.icons)),
                    FilteringSettings.CODEC.optionalFieldOf("filtering").forGetter(meta -> Optional.of(meta.filteringSettings)),
                    IntegritySettings.CODEC.optionalFieldOf("integrity").forGetter(meta -> Optional.of(meta.integritySettings))
            ).apply(instance, (name, modified, icons, filtering, integrity) -> new Metadata(
                    name.orElse(null),
                    modified.orElse(Instant.now()),
                    icons.orElseGet(() -> new ArrayList<>(MemoryKeyIcon.DEFAULT_ORDER)),
                    filtering.orElseGet(FilteringSettings::new),
                    integrity.orElseGet(IntegritySettings::new)
            ))
    );

    @Nullable
    private String name;
    private Instant lastModified;
    private final List<MemoryKeyIcon> icons;
    private final FilteringSettings filteringSettings;
    private final IntegritySettings integritySettings;

    public Metadata(@Nullable String name, Instant lastModified, List<MemoryKeyIcon> icons, FilteringSettings filteringSettings, IntegritySettings integritySettings) {
        this.name = name;
        this.lastModified = lastModified;
        this.icons = icons;
        this.filteringSettings = filteringSettings;
        this.integritySettings = integritySettings;
    }

    public static Metadata blank() {
        return new Metadata(null, Instant.now(), new ArrayList<>(MemoryKeyIcon.DEFAULT_ORDER), new FilteringSettings(), new IntegritySettings());
    }

    public static Metadata blankWithName(String name) {
        var blank = blank();
        blank.setName(name);
        return blank;
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

    public List<MemoryKeyIcon> getIcons() {
        return icons;
    }

    public FilteringSettings getFilteringSettings() {
        return filteringSettings;
    }

    public IntegritySettings getIntegritySettings() {
        return integritySettings;
    }

    public Metadata deepCopy() {
        return new Metadata(name, lastModified, new ArrayList<>(icons), filteringSettings.copy(), integritySettings.copy());
    }

    public static class FilteringSettings {
        private static final Codec<FilteringSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("onlyRememberNamed")
                            .forGetter(settings -> settings.onlyRememberNamed)
            ).apply(instance, FilteringSettings::new));

        public boolean onlyRememberNamed = false;

        private FilteringSettings() {}

        public FilteringSettings(boolean onlyRememberNamed) {
            this.onlyRememberNamed = onlyRememberNamed;
        }

        public FilteringSettings copy() {
            return new FilteringSettings(onlyRememberNamed);
        }
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

        private IntegritySettings() {}

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

        public IntegritySettings copy() {
            return new IntegritySettings(removeOnPlayerBlockBreak, checkPeriodicallyForMissingBlocks, memoryLifetime, preserveNamed);
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
