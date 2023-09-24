package red.jackf.chesttracker.util;

import net.minecraft.network.chat.MutableComponent;

import static net.minecraft.network.chat.Component.translatable;

public class I18nUtil {
    public static MutableComponent colon(Object key, Object value) {
        return translatable("options.generic_value", key, value);
    }

    public static MutableComponent seconds(int seconds) {
        return translatable("chesttracker.generic.seconds", seconds);
    }

    public static MutableComponent minutes(int minutes) {
        return translatable("chesttracker.generic.minutes", minutes);
    }

    public static MutableComponent hours(int hours) {
        if (hours == 1) return translatable("chesttracker.generic.hour");
        return translatable("chesttracker.generic.hours", hours);
    }

    public static MutableComponent days(int days) {
        if (days == 1) return translatable("chesttracker.generic.day");
        return translatable("chesttracker.generic.days", days);
    }

    public static MutableComponent blocks(Object value) {
        return translatable("chesttracker.generic.blocks", value);
    }
}
