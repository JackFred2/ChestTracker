package red.jackf.chesttracker.impl.memory.metadata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.impl.util.I18n;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

import java.util.Optional;

public class IntegritySettings {
    protected static final Codec<IntegritySettings> CODEC = RecordCodecBuilder.create(instance -> {
        final var def = new IntegritySettings();
        return instance.group(
                        Codec.BOOL.optionalFieldOf("removeOnPlayerBlockBreak")
                                .forGetter(settings -> Optional.of(settings.removeOnPlayerBlockBreak)),
                        Codec.BOOL.optionalFieldOf("checkPeriodicallyForMissingBlocks")
                                .forGetter(settings -> Optional.of(settings.checkPeriodicallyForMissingBlocks)),
                        JFLCodecs.forEnum(MemoryLifetime.class).optionalFieldOf("memoryLifetime")
                                .forGetter(settings -> Optional.of(settings.memoryLifetime)),
                        Codec.BOOL.optionalFieldOf("preserveNamed")
                                .forGetter(settings -> Optional.of(settings.preserveNamed)),
                        JFLCodecs.forEnum(LifetimeCountMode.class).optionalFieldOf("lifetimeCountMode")
                                .forGetter(settings -> Optional.of(settings.lifetimeCountMode))
                )
                .apply(instance, (removeOnPlayerBlockBreak, checkPeriodicallyForMissingBlocks, memoryLifetime, preserveNamed, lifetimeCountMode) ->
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

    IntegritySettings() {
    }

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
        REAL_TIME(Component.translatable("chesttracker.gui.editMemoryBank.integrity.lifetimeCountMode.realTime")),
        WORLD_TIME(Component.translatable("chesttracker.gui.editMemoryBank.integrity.lifetimeCountMode.worldTime")),
        LOADED_TIME(Component.translatable("chesttracker.gui.editMemoryBank.integrity.lifetimeCountMode.loadedTime"));

        public final Component label;

        LifetimeCountMode(Component label) {
            this.label = label;
        }
    }

    private static Component lifetimePrefix() {
        return Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime");
    }

    public enum MemoryLifetime {
        TEN_SECONDS(10L, I18n.colon(lifetimePrefix(), I18n.seconds(10))),
        FIVE_MINUTES(60L * 5L, I18n.colon(lifetimePrefix(), I18n.minutes(5))),
        TWENTY_MINUTES(60L * 15L, I18n.colon(lifetimePrefix(), I18n.minutes(15))),
        FORTY_MINUTES(60L * 30L, I18n.colon(lifetimePrefix(), I18n.minutes(30))),
        ONE_HOUR(60L * 60L, I18n.colon(lifetimePrefix(), I18n.hours(1))),
        TWO_HOURS(60L * 60L * 2L, I18n.colon(lifetimePrefix(), I18n.hours(2))),
        FOUR_HOURS(60L * 60L * 4L, I18n.colon(lifetimePrefix(), I18n.hours(4))),
        SIX_HOURS(60L * 60L * 6L, I18n.colon(lifetimePrefix(), I18n.hours(6))),
        TWELVE_HOURS(60L * 60L * 12L, I18n.colon(lifetimePrefix(), I18n.hours(12))),
        ONE_DAY(60L * 60L * 24L, I18n.colon(lifetimePrefix(), I18n.days(1))),
        TWO_DAYS(60L * 60L * 24L * 2L, I18n.colon(lifetimePrefix(), I18n.days(2))),
        FIVE_DAYS(60L * 60L * 24L * 5L, I18n.colon(lifetimePrefix(), I18n.days(5))),
        SEVEN_DAYS(60L * 60L * 24L * 7L, I18n.colon(lifetimePrefix(), I18n.days(7))),
        NEVER(null, I18n.colon(lifetimePrefix(), Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.never")));

        public final Long seconds;
        public final Component label;

        MemoryLifetime(@Nullable Long seconds, Component label) {
            this.seconds = seconds;
            this.label = label;
        }
    }
}
