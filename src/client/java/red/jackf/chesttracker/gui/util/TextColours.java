package red.jackf.chesttracker.gui.util;

/**
 * List of text colours used in the mod; sourced from assets/chesttracker/textures/gui/text_colours.png
 */
public class TextColours {
    private TextColours() {}

    private static int labelColour = 0x404040;
    private static int searchTextColour = 0xFFFFFF;
    private static int searchHintColour = 0x808080;
    private static int searchKeyColour = 0x669BBC;
    private static int searchTermColour = 0xEECC77;
    private static int searchErrorColour = 0xFF0000;

    public static int getLabelColour() {
        return labelColour;
    }

    static void setLabelColour(int labelColour) {
        TextColours.labelColour = labelColour;
    }

    public static int getSearchTextColour() {
        return searchTextColour;
    }

    static void setSearchTextColour(int searchTextColour) {
        TextColours.searchTextColour = searchTextColour;
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

    public static int getSearchErrorColour() {
        return searchErrorColour;
    }

    static void setSearchErrorColour(int searchErrorColour) {
        TextColours.searchErrorColour = searchErrorColour;
    }

    public static void setSearchHintColour(int searchHintColour) {
        TextColours.searchHintColour = searchHintColour;
    }

    public static int getSearchHintColour() {
        return searchHintColour;
    }
}
