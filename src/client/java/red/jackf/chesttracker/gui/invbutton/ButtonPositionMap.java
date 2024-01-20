package red.jackf.chesttracker.gui.invbutton;

import com.mojang.serialization.Codec;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.FileUtil;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ButtonPositionMap {
    private static final ButtonPosition FALLBACK_DEFAULT
            = new ButtonPosition(ButtonPosition.HorizontalAlignment.right, 14, ButtonPosition.VerticalAlignment.top, 5);
    private static final Path USER_PATH = Constants.STORAGE_DIR.resolve("user_button_positions.dat");
    private static final Codec<Map<String, ButtonPosition>> USER_CODEC = JFLCodecs.mutableMap(Codec.unboundedMap(
            Codec.STRING, ButtonPosition.CODEC
    ));

    private static final Map<String, ButtonPosition> datapackPositions = new HashMap<>();
    private static final Map<String, ButtonPosition> userPositions = new HashMap<>();

    public static void loadDatapackPositions(Map<String, ButtonPosition> datapack) {
        datapackPositions.clear();
        datapackPositions.putAll(datapack);
    }

    public static void loadUserPositions() {
        userPositions.clear();

        FileUtil.loadFromNbt(USER_CODEC, USER_PATH).ifPresent(userPositions::putAll);
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

        FileUtil.saveToNbt(userPositions, USER_CODEC, USER_PATH);
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
