package red.jackf.chesttracker.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

import java.util.function.Function;

import static red.jackf.chesttracker.config.ButtonDisplayTypeFunctions.*;

@Environment(EnvType.CLIENT)
public enum ButtonDisplayType {
    ABOVE_RIGHT(rightX, aboveY, false),
    ABOVE_LEFT(leftX, aboveY, false),
    TOP_RIGHT(rightX, topY, false),
    MIDDLE_RIGHT(FabricLoader.getInstance().isModLoaded("inventoryprofiles") ? rightXInvProfShift : rightX, middleY, false),
    BOTTOM_RIGHT(rightX, bottomRightY, false),
    BOTTOM_LEFT(leftX, bottomLeftY, false),
    TOP_LEFT_VERTICAL(leftVertX, topLeftVertY, true),
    BOTTOM_LEFT_VERTICAL(leftVertX, bottomLeftVertY, true);

    private final Function<HandledScreen<?>, Integer> getX;
    private final Function<HandledScreen<?>, Integer> getY;
    private final boolean vertical;

    ButtonDisplayType(Function<HandledScreen<?>, Integer> getX, Function<HandledScreen<?>, Integer> getY, boolean vertical) {
        this.getX = getX;
        this.getY = getY;
        this.vertical = vertical;
    }

    public int getX(HandledScreen<?> screen) {
        return getX.apply(screen);
    }

    public int getY(HandledScreen<?> screen) {
        return getY.apply(screen);
    }

    public boolean isVertical() {
        return vertical;
    }

    public static ButtonDisplayType getAppropriateDefault() {
        FabricLoader loader = FabricLoader.getInstance();
        if (loader.isModLoaded("inventoryprofiles")) {
            if (loader.isModLoaded("techreborn")) {
                return BOTTOM_LEFT_VERTICAL;
            } else {
                return TOP_LEFT_VERTICAL;
            }
        } else {
            return TOP_RIGHT;
        }
    }
}
