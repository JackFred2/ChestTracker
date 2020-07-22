package red.jackf.chesttracker.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum InteractRememberType {
    BLOCK_ENTITIES,
    ALL
}
