package red.jackf.chesttracker.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;

import java.util.Optional;

public abstract class AppliedEnergisticsHandler {
    @Nullable
    private static Class<?> SLOT_CLASS = null;

    static {
        if (FabricLoader.getInstance().isModLoaded("appliedenergistics2")) {
            var yarnName = getClassIfExists("appeng.container.slot.AppEngSlot");
            if (yarnName.isEmpty()) {
                var mojangName = getClassIfExists("appeng.menu.slot.AppEngSlot");
                if (mojangName.isPresent()) {
                    SLOT_CLASS = mojangName.get();
                } else {
                    ChestTracker.LOGGER.warn("AE2 loaded but could not find slot class.");
                }
            } else {
                SLOT_CLASS = yarnName.get();
            }
        }
    }

    private static Optional<Class<?>> getClassIfExists(String name) {
        try {
            return Optional.of(Class.forName(name));
        } catch (ClassNotFoundException ex) {
            return Optional.empty();
        }
    }

    public static boolean isAE2Slot(Slot slot) {
        return SLOT_CLASS != null && SLOT_CLASS.isInstance(slot);
    }
}
