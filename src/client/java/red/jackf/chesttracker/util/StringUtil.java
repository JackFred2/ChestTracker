package red.jackf.chesttracker.util;

public class StringUtil {
    private static final Character[] siSuffixes = new Character[]{'k', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y', 'R', 'Q'};

    private static String magnitude(long in, int decimalPlaces, boolean withSpace) {
        double total = in;
        int index = -1;
        while (total >= 1000) {
            total /= 1000;
            index++;
        }
        if (index == -1)
            return String.valueOf(in);
        // god help you
        index = Math.min(index, siSuffixes.length - 1);
        var formatStr = "%." + decimalPlaces + "f";
        if (withSpace) formatStr += " ";
        formatStr += "%s";
        return formatStr.formatted(total, siSuffixes[index]);
    }

    public static String magnitudeSpace(long in, int decimalPlaces) {
        return magnitude(in, decimalPlaces, true);
    }

    public static String magnitude(long in, int decimalPlaces) {
        return magnitude(in, decimalPlaces, false);
    }

    public static String commaSeparated(int number) {
        return "%,d".formatted(number);
    }
}
