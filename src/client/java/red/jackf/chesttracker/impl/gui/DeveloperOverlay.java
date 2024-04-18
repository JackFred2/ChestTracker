package red.jackf.chesttracker.impl.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import red.jackf.chesttracker.api.memory.MemoryKey;
import red.jackf.chesttracker.api.providers.InteractionTracker;
import red.jackf.chesttracker.api.providers.ProviderUtils;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;
import red.jackf.chesttracker.impl.providers.ProviderHandler;
import red.jackf.jackfredlib.client.api.gps.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeveloperOverlay {
    public static void setup() {
        HudRenderCallback.EVENT.register((graphics, delta) -> {
            var provider = ProviderHandler.INSTANCE.getCurrentProvider().orElse(null);

            if (!ChestTrackerConfig.INSTANCE.instance().debug.showDevHud) return;
            List<String> lines = new ArrayList<>();
            lines.add("Chest Tracker Debug");
            lines.add("");
            lines.add("Coordinate: " + Coordinate.getCurrent().orElse(null));
            lines.add("");
            lines.add("Provider: " + (provider != null ? provider.id() : "<none>"));
            lines.add("");
            MemoryBankAccessImpl.INSTANCE.getLoadedInternal().ifPresentOrElse(bank -> {
                var currentKey = ProviderUtils.getPlayersCurrentKey();
                lines.add("Storage Backend: " + ChestTrackerConfig.INSTANCE.instance().storage.storageBackend.toString());
                var loadedStr = "Loaded: " + bank.getId();
                if (bank.getMetadata().getName() != null)
                    loadedStr += " (" + bank.getMetadata().getName() + ")";
                lines.add(loadedStr);
                lines.add("Keys: " + bank.getKeys().size());
                lines.add("Current key: " + currentKey);
                if (currentKey.isPresent()) {
                    Optional<MemoryKey> currentMemoryKey = bank.getKey(currentKey.get());
                    if (currentMemoryKey.isPresent())
                        lines.add("Memories in current key: " + currentMemoryKey.get().getMemories().size());
                    else
                        lines.add("No memories in current key");
                }
                lines.add("");
                var source = InteractionTracker.INSTANCE.getLastBlockSource();
                var sourceStr = source.map(blockSource -> blockSource.pos()
                        .toShortString() + "@" + blockSource.level()
                        .dimension().location()).orElse("<none>");
                lines.add("Location: " + sourceStr);
            }, () -> {
                lines.add("No memory bank loaded");
            });


            for (int i = 0; i < lines.size(); i++) {
                var line = lines.get(i);
                graphics.drawString(Minecraft.getInstance().font, line, 10, 10 + (9 * i), 0xFF_FFFFFF);
            }
        });
    }
}
