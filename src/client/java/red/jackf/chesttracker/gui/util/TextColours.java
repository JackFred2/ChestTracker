package red.jackf.chesttracker.gui.util;

public class TextColours {
    private TextColours() {}

    private static int titleColour = 0x404040;
    private static int searchTextColour = 0xFFFFFF;
    private static int searchKeyColour = 0x669BBC;
    private static int searchTermColour = 0xEECC77;
    private static int searchErrorColour = 0xFF0000;

    public static int getTitleColour() {
        return titleColour;
    }

    static void setTitleColour(int titleColour) {
        TextColours.titleColour = titleColour;
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
}
