package red.jackf.chesttracker.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import red.jackf.chesttracker.api.provider.InteractionTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.provider.ProviderHandler;

import java.util.ArrayList;
import java.util.List;

public class DeveloperOverlay {
    public static void setup() {
        HudRenderCallback.EVENT.register((graphics, delta) -> {
            if (!ChestTrackerConfig.INSTANCE.instance().debug.showDevHud) return;
            List<String> lines = new ArrayList<>();
            lines.add("Chest Tracker Debug");
            lines.add("");
            lines.add("Provider: " + (ProviderHandler.INSTANCE != null ? String.valueOf(ProviderHandler.INSTANCE.name()) : "<none>"));
            lines.add("");
            if (MemoryBank.INSTANCE != null) {
                lines.add("Storage Backend: " + ChestTrackerConfig.INSTANCE.instance().storage.storageBackend.toString());
                var loadedStr = "Loaded: " + MemoryBank.INSTANCE.getId();
                if (MemoryBank.INSTANCE.getMetadata().getName() != null)
                    loadedStr += " (" + MemoryBank.INSTANCE.getMetadata().getName() + ")";
                lines.add(loadedStr);
                lines.add("Keys: " + MemoryBank.INSTANCE.getKeys().size());
                var level = Minecraft.getInstance().level;
                if (level != null) {
                    lines.add("Current key: " + ProviderHandler.getCurrentKey());
                    var currentLevelMemories = MemoryBank.INSTANCE.getMemories(level.dimension().location());
                    if (currentLevelMemories != null)
                        lines.add("Memories in current dimension: " + currentLevelMemories.size());
                    else
                        lines.add("No memories in current dimension");
                }
                lines.add("");
                var source = InteractionTracker.INSTANCE.getLastBlockSource();
                var sourceStr = source.map(blockSource -> blockSource.pos().toShortString() + "@" + blockSource.level()
                        .dimension().location()).orElse("<none>");
                lines.add("Location: " + sourceStr);
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
