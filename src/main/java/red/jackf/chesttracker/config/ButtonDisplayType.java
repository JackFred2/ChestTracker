package red.jackf.chesttracker.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum ButtonDisplayType {
    AUTO,
    FORCE_BIG,
    FORCE_SMALL
}
