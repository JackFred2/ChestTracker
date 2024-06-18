package red.jackf.chesttracker.impl.gui.invbutton;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import red.jackf.chesttracker.impl.gui.invbutton.position.ButtonPosition;
import red.jackf.chesttracker.impl.util.Constants;
import red.jackf.chesttracker.impl.util.FileUtil;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles storage of button positions - both user preferences stored in 'chesttracker/user_button_positions.dat' and
 * datapack positions
 */
public class ButtonPositionMap {
    private static final ButtonPosition FALLBACK_DEFAULT
            = new ButtonPosition(ButtonPosition.HorizontalAlignment.right, 14, ButtonPosition.VerticalAlignment.top, 5);
    private static final Path USER_PATH = Constants.STORAGE_DIR.resolve("user_button_positions.dat");
    protected static final Codec<Map<String, ButtonPosition>> USER_CODEC = JFLCodecs.mutableMap(Codec.unboundedMap(
            Codec.STRING, ButtonPosition.CODEC
    ));

    private static final Map<String, ButtonPosition> datapackPositions = new HashMap<>();
    private static final Map<String, ButtonPosition> userPositions = new HashMap<>();

    /**
     * Receive all positions defined by a datapack.
     */
    public static void loadDatapackPositions(Map<String, ButtonPosition> datapack) {
        datapackPositions.clear();
        datapackPositions.putAll(datapack);
    }

    /**
     * Load all positions from the 'user_button_positions.dat' file.
     */
    public static void loadUserPositions() {
        userPositions.clear();

        FileUtil.loadFromNbt(USER_CODEC, USER_PATH, null).ifPresent(userPositions::putAll);
    }

    /**
     * Return a read-only copy of the user position map.
     */
    public static Map<String, ButtonPosition> getUserPositions() {
        return ImmutableMap.copyOf(userPositions);
    }

    /**
     * Remove a given screen from the user preference map.
     */
    public static void removeUserPosition(String position) {
        boolean result = userPositions.remove(position) != null;
        if (result) saveUserPositions();
    }

    /**
     * Save the user position file.
     */
    private static void saveUserPositions() {
        FileUtil.saveToNbt(userPositions, USER_CODEC, USER_PATH, null);
    }

    /**
     * Returns the class name for a screen, mapped to intermediary if possible.
     */
    public static String getClassString(AbstractContainerScreen<?> screen) {
        String className = screen.getClass().getCanonicalName();

        // prefer intermediary if we can in case we're in a dev env

        MappingResolver mapper = FabricLoader.getInstance().getMappingResolver();
        if (!mapper.getCurrentRuntimeNamespace().equals("intermediary") && mapper.getNamespaces().contains("intermediary")) {
            className = mapper.unmapClassName("intermediary", className);
        }

        return className;
    }

    /**
     * Save a given user preferred position and update the user preference file.
     */
    public static void saveUserPosition(AbstractContainerScreen<?> screen, ButtonPosition userPosition) {
        String className = getClassString(screen);

        ButtonPosition datapackPosition = datapackPositions.getOrDefault(className, null);
        if (userPosition.equals(datapackPosition)) { // if moved back to default, don't store
            userPositions.remove(className);
        } else {
            userPositions.put(className, userPosition);
        }

        saveUserPositions();
    }

    /**
     * Gets a button position for a given screen. Preference is:
     * <ol>
     *     <li>User Custom Position</li>
     *     <li>Datapack Position</li>
     *     <li>Datapack Fallback Position</li>
     *     <li>{@link #FALLBACK_DEFAULT}</li>
     * </ol>
     */
    public static ButtonPosition getPositionFor(AbstractContainerScreen<?> screen) {
        String className = getClassString(screen);

        if (userPositions.containsKey(className)) {
            return userPositions.get(className);
        }

        if (datapackPositions.containsKey(className)) {
            return datapackPositions.get(className);
        }

        // return the default, or in case nothing's loaded the fallback
        return datapackPositions.getOrDefault("DEFAULT", FALLBACK_DEFAULT);
    }
}
