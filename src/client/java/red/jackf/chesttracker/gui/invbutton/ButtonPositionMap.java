package red.jackf.chesttracker.gui.invbutton;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.HashMap;
import java.util.Map;

public class ButtonPositionMap {
    private static final ButtonPosition FALLBACK_DEFAULT
            = new ButtonPosition(ButtonPosition.HorizontalAlignment.right, 14, ButtonPosition.VerticalAlignment.top, 5);

    private static final Map<String, ButtonPosition> datapackPositions = new HashMap<>();
    private static final Map<String, ButtonPosition> userPositions = new HashMap<>();

    public static void loadDatapackPositions(Map<String, ButtonPosition> datapack) {
        datapackPositions.clear();
        datapackPositions.putAll(datapack);
    }

    private static String getClassString(AbstractContainerScreen<?> screen) {
        String className = screen.getClass().getCanonicalName();

        // prefer intermediary if we can in case we're in a dev env

        var mapper = FabricLoader.getInstance().getMappingResolver();
        if (!mapper.getCurrentRuntimeNamespace().equals("intermediary") && mapper.getNamespaces().contains("intermediary")) {
            className = mapper.unmapClassName("intermediary", className);
        }

        return className;
    }

    public static void setUser(AbstractContainerScreen<?> screen, ButtonPosition position) {
        String className = getClassString(screen);

        userPositions.put(className, position);
    }

    public static ButtonPosition getFor(AbstractContainerScreen<?> screen) {
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
