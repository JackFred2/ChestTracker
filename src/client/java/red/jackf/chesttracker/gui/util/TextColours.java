package red.jackf.chesttracker.gui.util;

/**
 * List of text colours used in the mod; sourced from assets/chesttracker/textures/gui/text_colours.png
 */
public class TextColours {
    private TextColours() {
    }

    private static int labelColour = 0x404040;
    private static int textColour = 0xFFFFFF;
    private static int hintColour = 0x808080;
    private static int searchKeyColour = 0x669BBC;
    private static int searchTermColour = 0xEECC77;
    private static int errorColour = 0xFF0000;

    public static int getLabelColour() {
        return labelColour;
    }

    static void setLabelColour(int labelColour) {
        TextColours.labelColour = labelColour;
    }

    public static int getTextColour() {
        return textColour;
    }

    static void setTextColour(int textColour) {
        TextColours.textColour = textColour;
    }

    public static int getSearchKeyColour() {
        return searchKeyColour;
    }

    static void setSearchKeyColour(int searchKeyColour) {
        TextColours.searchKeyColour = searchKeyColour;
    }

    public static int getSearchTermColour() {
        return searchTermColour;
    }

    static void setSearchTermColour(int searchTermColour) {
        TextColours.searchTermColour = searchTermColour;
    }

    public static int getErrorColour() {
        return errorColour;
    }

    static void setErrorColour(int errorColour) {
        TextColours.errorColour = errorColour;
    }

    public static void setHintColour(int hintColour) {
        TextColours.hintColour = hintColour;
    }

    public static int getHintColour() {
        return hintColour;
    }
}
