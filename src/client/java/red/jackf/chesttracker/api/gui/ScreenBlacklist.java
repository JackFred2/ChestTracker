package red.jackf.chesttracker.api.gui;

import net.minecraft.client.gui.screens.Screen;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ScreenBlacklist {
    private static final Set<Class<? extends Screen>> BLACKLIST = new HashSet<>();

    @SafeVarargs
    public static void add(Class<? extends Screen>... screenClasses) {
        BLACKLIST.addAll(Arrays.asList(screenClasses));
    }

    public static boolean isBlacklisted(Class<? extends Screen> screenClass) {
        return BLACKLIST.stream().anyMatch(blacklisted -> blacklisted.isAssignableFrom(screenClass));
    }
}
