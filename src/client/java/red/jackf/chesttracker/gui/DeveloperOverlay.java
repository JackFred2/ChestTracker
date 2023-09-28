package red.jackf.chesttracker.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.location.LocationTracking;

import java.util.ArrayList;
import java.util.List;

public class DeveloperOverlay {
    public static void setup() {
        HudRenderCallback.EVENT.register((graphics, delta) -> {
            if (!ChestTrackerConfig.INSTANCE.getConfig().gui.showDevHud) return;
            List<String> lines = new ArrayList<>();
            lines.add("Chest Tracker Debug");
            lines.add("");
            if (MemoryBank.INSTANCE != null) {
                lines.add("Storage Backend: " + ChestTrackerConfig.INSTANCE.getConfig().storage.storageBackend.toString());
                var loadedStr = "Loaded: " + MemoryBank.INSTANCE.getId();
                if (MemoryBank.INSTANCE.getMetadata().getName() != null)
                    loadedStr += " (" + MemoryBank.INSTANCE.getMetadata().getName() + ")";
                lines.add(loadedStr);
                lines.add("Keys: " + MemoryBank.INSTANCE.getKeys().size());
                var level = Minecraft.getInstance().level;
                if (level != null) {
                    lines.add("Current dimension: " + level.dimension().location());
                    var currentLevelMemories = MemoryBank.INSTANCE.getMemories(level.dimension().location());
                    if (currentLevelMemories != null)
                        lines.add("Memories in current dimension: " + currentLevelMemories.size());
                    else
                        lines.add("No memories in current dimension");
                }
                lines.add("");
                var loc = LocationTracking.peekLocation();
                var locStr = loc == null ? "<none>" : loc.pos().toShortString() + "@" + loc.key();
                lines.add("Location: " + locStr);
            } else {
                lines.add("No memory bank loaded");
            }


            for (int i = 0; i < lines.size(); i++) {
                var line = lines.get(i);
                graphics.drawString(Minecraft.getInstance().font, line, 10, 10 + (9 * i), 0xFF_FFFFFF);
            }
        });
    }
}
