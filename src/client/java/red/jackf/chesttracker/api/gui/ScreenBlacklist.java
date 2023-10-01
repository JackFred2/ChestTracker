package red.jackf.chesttracker.api.gui;

import net.minecraft.client.gui.screens.Screen;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Screens can be blacklisted to globally prevent them from being tracked, this prevents edge cases such as interacting
 * with a blocked chest then opening the creative inventory. Any screen classes added to the blacklist will not be tracked,
 * as well as any subclasses.</p>
 * <p>By default, this includes the two inventory screens (survival and creative).</p>
 */
public class ScreenBlacklist {
    private static final Set<Class<? extends Screen>> BLACKLIST = new HashSet<>();

    /**
     * Add screen classes to the global blacklist. This includes any subclasses.
     *
     * @param screenClasses Screen classes to add to the blacklist, including subclasses.
     */
    @SafeVarargs
    public static void add(Class<? extends Screen>... screenClasses) {
        BLACKLIST.addAll(Arrays.asList(screenClasses));
    }

    /**
     * Checks whether a given screen class is on the global blacklist.
     * @param screenClass Screen class to check.
     * @return If the given screen class is on the blacklist.
     */
    public static boolean isBlacklisted(Class<? extends Screen> screenClass) {
        return BLACKLIST.stream().anyMatch(blacklisted -> blacklisted.isAssignableFrom(screenClass));
    }

    private ScreenBlacklist() {}
}
