package red.jackf.chesttracker;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.ChestTrackerScreen;
import red.jackf.whereisit.client.api.ShouldIgnoreKey;

@Environment(EnvType.CLIENT)
public class ChestTracker implements ClientModInitializer {
    private static final String ID = "chesttracker";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final KeyMapping OPEN_GUI = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.chesttracker.open_gui", InputConstants.Type.KEYSYM, InputConstants.KEY_GRAVE, "chesttracker.title")
    );

    @Override
    public void onInitializeClient() {
        try {
            ChestTrackerConfig.INSTANCE.load();
            ChestTrackerConfig.INSTANCE.getConfig().validate();
        } catch (Exception ex) {
            LOGGER.error("Error loading Chest Tracker config, restoring default", ex);
        }
        ChestTrackerConfig.INSTANCE.save();
        LOGGER.debug("Loading ChestTracker");

        setupKeybinds();
    }

    private void setupKeybinds() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.screen == null && client.getOverlay() == null)
                while (OPEN_GUI.consumeClick())
                    client.setScreen(new ChestTrackerScreen(null));
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?>)
                ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                    if (ShouldIgnoreKey.EVENT.invoker().shouldIgnoreKey()) {
                        return;
                    }

                    if (OPEN_GUI.matches(key, scancode))
                        client.setScreen(new ChestTrackerScreen(screen1));
                });
        });
    }
}
