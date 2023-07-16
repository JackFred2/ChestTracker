package red.jackf.chesttracker.util;

public class Magnitudes {
    private static final Character[] siSuffixes = new Character[]{'k', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y', 'R', 'Q'};

    public static String format(long in, int decimalPlaces, String suffix) {
        double total = in;
        int index = -1;
        while (total >= 1000) {
            total /= 1000;
            index++;
        }
        if (index == -1) return in + " " + suffix;
        index = Math.min(index, siSuffixes.length - 1);
        var formatStr = "%." + decimalPlaces + "f %s%s";
        return formatStr.formatted(total, siSuffixes[index], suffix);
    }

    public static String format(long in, int decimalPlaces) {
        return format(in, decimalPlaces, "");
    }
}
