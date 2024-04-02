package red.jackf.chesttracker.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import red.jackf.chesttracker.api.provider.InteractionTracker;
import red.jackf.chesttracker.api.provider.Provider;
import red.jackf.chesttracker.compat.servers.hypixel.HypixelProvider;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.memory.key.MemoryKey;
import red.jackf.chesttracker.provider.ProviderHandler;
import red.jackf.jackfredlib.client.api.gps.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class DeveloperOverlay {
    public static void setup() {
        HudRenderCallback.EVENT.register((graphics, delta) -> {
            Provider provider = ProviderHandler.INSTANCE;

            if (!ChestTrackerConfig.INSTANCE.instance().debug.showDevHud) return;
            List<String> lines = new ArrayList<>();
            lines.add("Chest Tracker Debug");
            lines.add("");
            lines.add("Coordinate: " + Coordinate.getCurrent().orElse(null));
            lines.add("");
            lines.add("Provider: " + (provider != null ? String.valueOf(provider.name()) : "<none>"));
            if (provider != null) {
                if (provider == HypixelProvider.INSTANCE) {
                    lines.add("isOnSkyblock: " + HypixelProvider.isOnSkyblock());
                    lines.add("isOnSmp: " + ((HypixelProvider) provider).isOnSmp);
                }
            }
            lines.add("");
            if (MemoryBank.INSTANCE != null) {
                var currentKey = ProviderHandler.getCurrentKey();
                lines.add("Storage Backend: " + ChestTrackerConfig.INSTANCE.instance().storage.storageBackend.toString());
                var loadedStr = "Loaded: " + MemoryBank.INSTANCE.getId();
                if (MemoryBank.INSTANCE.getMetadata().getName() != null)
                    loadedStr += " (" + MemoryBank.INSTANCE.getMetadata().getName() + ")";
                lines.add(loadedStr);
                lines.add("Keys: " + MemoryBank.INSTANCE.getKeys().size());
                var level = Minecraft.getInstance().level;
                if (level != null) {
                    lines.add("Current key: " + currentKey);
                    if (currentKey != null) {
                        MemoryKey currentMemoryKey = MemoryBank.INSTANCE.getMemories(currentKey);
                        if (currentMemoryKey != null)
                            lines.add("Memories in current dimension: " + currentMemoryKey.memories().size());
                        else
                            lines.add("No memories in current dimension");
                    }
                }
                lines.add("");
                var source = InteractionTracker.INSTANCE.getLastBlockSource();
                var sourceStr = source.map(blockSource -> blockSource.pos()
                        .toShortString() + "@" + blockSource.level()
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
