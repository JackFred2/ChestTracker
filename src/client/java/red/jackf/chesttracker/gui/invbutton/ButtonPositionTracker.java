package red.jackf.chesttracker.gui.invbutton;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.HashMap;
import java.util.Map;

public enum ButtonPositionTracker {
    INSTANCE;

    private static final ButtonPosition DEFAULT = new ButtonPosition(ButtonPosition.HorizontalAlignment.right, 14, ButtonPosition.VerticalAlignment.top, 5);

    private final Map<String, ButtonPosition> datapackPositions = new HashMap<>();

    public void loadDatapackPositions(Map<String, ButtonPosition> datapack) {
        this.datapackPositions.clear();
        this.datapackPositions.putAll(datapack);
    }

    public ButtonPosition getFor(AbstractContainerScreen<?> screen) {
        var mapping = FabricLoader.getInstance().getMappingResolver();
        String className = screen.getClass().getCanonicalName();

        // prefer intermediary if we can in case we're in a dev env

        if (!mapping.getCurrentRuntimeNamespace().equals("intermediary") && mapping.getNamespaces().contains("intermediary")) {
            className = mapping.unmapClassName("intermediary", className);
        }

        // todo lookup user pref

        if (datapackPositions.containsKey(className)) {
            return datapackPositions.get(className);
        }

        return DEFAULT;
    }
}
