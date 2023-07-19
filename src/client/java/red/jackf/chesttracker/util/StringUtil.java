package red.jackf.chesttracker.util;

import net.minecraft.SharedConstants;

import java.util.regex.Pattern;

public class StringUtil {
    private static final Character[] siSuffixes = new Character[]{'k', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y', 'R', 'Q'};
    private static final Pattern RESERVED_WINDOWS_FILENAMES = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?", Pattern.CASE_INSENSITIVE);

    private static String magnitude(long in, int decimalPlaces, boolean withSpace) {
        double total = in;
        int index = -1;
        while (total >= 1000) {
            total /= 1000;
            index++;
        }
        if (index == -1)
            return in + (withSpace ? " " : "");
        // god help you
        index = Math.min(index, siSuffixes.length - 1);
        var formatStr = "%." + decimalPlaces + "f";
        if (withSpace) formatStr += " ";
        formatStr += "%s";
        return formatStr.formatted(total, siSuffixes[index]);
    }

    /**
     * Formats a string with an SI magnitude suffix, with a space <br />
     * Example: magnitude(1_438_295, 2) -> 1.44 M <br />
     * Example: magnitude(2_000_000_050, 0) -> 2 B
     */
    public static String magnitudeSpace(long in, int decimalPlaces) {
        return magnitude(in, decimalPlaces, true);
    }

    /**
     * Formats a string with an SI magnitude suffix, without a space <br />
     * Example: magnitude(1_438_295, 2) -> 1.44M <br />
     * Example: magnitude(2_000_000_050, 0) -> 2B
     */
    public static String magnitude(long in, int decimalPlaces) {
        return magnitude(in, decimalPlaces, false);
    }

    public static String commaSeparated(int number) {
        return "%,d".formatted(number);
    }

    /**
     * Makes a string of text safe to be used as a windows file
     * adjusted from net.minecraft.FileUtil
     */
    public static String sanitize(String text) {
        if (text == null) return null;
        char[] var3 = SharedConstants.ILLEGAL_FILE_CHARACTERS;

        for (char c : var3) {
            text = text.replace(c, '_');
        }

        text = text.replaceAll("[./\"]", "_");
        if (RESERVED_WINDOWS_FILENAMES.matcher(text).matches()) {
            text = "_" + text + "_";
        }

        return text;
    }
}
