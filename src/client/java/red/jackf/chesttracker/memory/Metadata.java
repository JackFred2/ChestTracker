package red.jackf.chesttracker.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.gui.GuiConstants;
import red.jackf.chesttracker.gui.MemoryKeyIcon;
import red.jackf.chesttracker.util.ModCodecs;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class Metadata {
    public static final Codec<Metadata> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("name").forGetter(meta -> Optional.ofNullable(meta.name)),
                    ModCodecs.INSTANT.optionalFieldOf("lastModified").forGetter(meta -> Optional.of(meta.lastModified)),
                    Codec.LONG.fieldOf("loadedTime").forGetter(meta -> meta.loadedTime),
                    ModCodecs.makeMutableList(MemoryKeyIcon.CODEC.listOf()).optionalFieldOf("icons").forGetter(meta -> Optional.of(meta.icons)),
                    FilteringSettings.CODEC.optionalFieldOf("filtering").forGetter(meta -> Optional.of(meta.filteringSettings)),
                    IntegritySettings.CODEC.optionalFieldOf("integrity").forGetter(meta -> Optional.of(meta.integritySettings))
            ).apply(instance, (name, lastModified, loadedTime, icons, filtering, integrity) -> new Metadata(
                    name.orElse(null),
                    lastModified.orElse(Instant.now()),
                    loadedTime,
                    icons.orElseGet(() -> new ArrayList<>(GuiConstants.DEFAULT_ICONS)),
                    filtering.orElseGet(FilteringSettings::new),
                    integrity.orElseGet(IntegritySettings::new)
            ))
    );

    @Nullable
    private String name;
    private Instant lastModified;
    private long loadedTime;
    private final List<MemoryKeyIcon> icons;
    private final FilteringSettings filteringSettings;
    private final IntegritySettings integritySettings;

    public Metadata(@Nullable String name, Instant lastModified, long loadedTime, List<MemoryKeyIcon> icons, FilteringSettings filteringSettings, IntegritySettings integritySettings) {
        this.name = name;
        this.lastModified = lastModified;
        this.icons = icons;
        this.loadedTime = loadedTime;
        this.filteringSettings = filteringSettings;
        this.integritySettings = integritySettings;
    }

    public static Metadata blank() {
        return new Metadata(null, Instant.now(), 0L, new ArrayList<>(GuiConstants.DEFAULT_ICONS), new FilteringSettings(), new IntegritySettings());
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

    public LightweightStack getIcon(ResourceLocation key) {
        for (MemoryKeyIcon icon : icons) {
            if (icon.id().equals(key)) return icon.icon();
        }
        return new LightweightStack(GuiConstants.DEFAULT_ICON_ITEM, null);
    }

    public void setIcon(ResourceLocation key, LightweightStack icon) {
        var existingIndex = IntStream.range(0, icons.size())
                .filter(index -> icons.get(index).id().equals(key))
                .findFirst();
        var keyIcon = new MemoryKeyIcon(key, icon);
        if (existingIndex.isPresent()) {
            icons.set(existingIndex.getAsInt(), keyIcon);
        } else {
            icons.add(keyIcon);
        }
    }

    public long getLoadedTime() {
        return loadedTime;
    }

    public FilteringSettings getFilteringSettings() {
        return filteringSettings;
    }

    public IntegritySettings getIntegritySettings() {
        return integritySettings;
    }

    public Metadata deepCopy() {
        return new Metadata(name, lastModified, loadedTime, new ArrayList<>(icons), filteringSettings.copy(), integritySettings.copy());
    }

    public void incrementLoadedTime() {
        this.loadedTime++;
    }

    public static class FilteringSettings {
        private static final Codec<FilteringSettings> CODEC = RecordCodecBuilder.create(instance -> {
            final var def = new FilteringSettings();
            return instance.group(
                    Codec.BOOL.optionalFieldOf("onlyRememberNamed")
                            .forGetter(settings -> Optional.of(settings.onlyRememberNamed))
            ).apply(instance, (onlyRememberNamed) ->
                    new FilteringSettings(
                            onlyRememberNamed.orElse(def.onlyRememberNamed)
                    ));
        });

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
        private static final Codec<IntegritySettings> CODEC = RecordCodecBuilder.create(instance -> {
            final var def = new IntegritySettings();
            return instance.group(
                    Codec.BOOL.optionalFieldOf("removeOnPlayerBlockBreak")
                            .forGetter(settings -> Optional.of(settings.removeOnPlayerBlockBreak)),
                    Codec.BOOL.optionalFieldOf("checkPeriodicallyForMissingBlocks")
                            .forGetter(settings -> Optional.of(settings.checkPeriodicallyForMissingBlocks)),
                    ModCodecs.ofEnum(MemoryLifetime.class).optionalFieldOf("memoryLifetime")
                            .forGetter(settings -> Optional.of(settings.memoryLifetime)),
                    Codec.BOOL.optionalFieldOf("preserveNamed")
                            .forGetter(settings -> Optional.of(settings.preserveNamed)),
                    ModCodecs.ofEnum(LifetimeCountMode.class).optionalFieldOf("lifetimeCountMode")
                            .forGetter(settings -> Optional.of(settings.lifetimeCountMode))
            ).apply(instance, (removeOnPlayerBlockBreak, checkPeriodicallyForMissingBlocks, memoryLifetime, preserveNamed, lifetimeCountMode) ->
                    new IntegritySettings(
                            removeOnPlayerBlockBreak.orElse(def.removeOnPlayerBlockBreak),
                            checkPeriodicallyForMissingBlocks.orElse(def.checkPeriodicallyForMissingBlocks),
                            memoryLifetime.orElse(def.memoryLifetime),
                            preserveNamed.orElse(def.preserveNamed),
                            lifetimeCountMode.orElse(def.lifetimeCountMode)
                    ));
        });

        public boolean removeOnPlayerBlockBreak = true;
        public boolean checkPeriodicallyForMissingBlocks = true;
        public MemoryLifetime memoryLifetime = MemoryLifetime.TWELVE_HOURS;
        public boolean preserveNamed = true;
        public LifetimeCountMode lifetimeCountMode = LifetimeCountMode.LOADED_TIME;

        private IntegritySettings() {}

        public IntegritySettings(boolean removeOnPlayerBlockBreak,
                                 boolean checkPeriodicallyForMissingBlocks,
                                 MemoryLifetime memoryLifetime,
                                 boolean preserveNamed,
                                 LifetimeCountMode lifetimeCountMode) {
            this();
            this.removeOnPlayerBlockBreak = removeOnPlayerBlockBreak;
            this.checkPeriodicallyForMissingBlocks = checkPeriodicallyForMissingBlocks;
            this.memoryLifetime = memoryLifetime;
            this.preserveNamed = preserveNamed;
            this.lifetimeCountMode = lifetimeCountMode;
        }

        public IntegritySettings copy() {
            return new IntegritySettings(removeOnPlayerBlockBreak, checkPeriodicallyForMissingBlocks, memoryLifetime, preserveNamed, lifetimeCountMode);
        }

        public enum LifetimeCountMode {
            REAL_TIME(Component.translatable("chesttracker.gui.editMemoryBank.integrity.lifetimeCountMode.real_time")),
            WORLD_TIME(Component.translatable("chesttracker.gui.editMemoryBank.integrity.lifetimeCountMode.world_time")),
            LOADED_TIME(Component.translatable("chesttracker.gui.editMemoryBank.integrity.lifetimeCountMode.loaded_time"));

            public final Component label;

            LifetimeCountMode(Component label) {
                this.label = label;
            }
        }

        public enum MemoryLifetime {
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
            SEVEN_DAYS(60L * 60L * 24L * 7L, Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.days", 7)),
            NEVER(null, Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.never"));

            public final Long seconds;
            public final Component label;

            MemoryLifetime(@Nullable Long seconds, Component label) {
                this.seconds = seconds;
                this.label = label;
            }
        }
    }
}
