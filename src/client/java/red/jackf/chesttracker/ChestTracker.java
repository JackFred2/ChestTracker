package red.jackf.chesttracker;

import blue.endless.jankson.annotation.Nullable;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.api.gui.GetMemory;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.screen.ChestTrackerScreen;
import red.jackf.chesttracker.gui.DeveloperOverlay;
import red.jackf.chesttracker.gui.GuiApiDefaults;
import red.jackf.chesttracker.gui.util.ImagePixelReader;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.memory.MemoryIntegrity;
import red.jackf.chesttracker.storage.ConnectionSettings;
import red.jackf.chesttracker.storage.Storage;
import red.jackf.chesttracker.world.LocationTracking;
import red.jackf.whereisit.client.api.ShouldIgnoreKey;

public class ChestTracker implements ClientModInitializer {
    public static final String ID = "chesttracker";

    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
    }
    public static ResourceLocation guiTex(String path) {
        return new ResourceLocation(ID, "textures/gui/" + path + ".png");
    }
    public static final Logger LOGGER = LogManager.getLogger();
    public static Logger getLogger(String suffix) {
        return LogManager.getLogger(ChestTracker.class.getCanonicalName() + "/" + suffix);
    }

    public static final KeyMapping OPEN_GUI = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.chesttracker.open_gui", InputConstants.Type.KEYSYM, InputConstants.KEY_GRAVE, "chesttracker.title")
    );

    private void openInGame(Minecraft client, @Nullable Screen parent) {
        client.setScreen(new ChestTrackerScreen(parent));
    }

    @Override
    public void onInitializeClient() {
        ChestTrackerConfig.init();
        LOGGER.debug("Loading ChestTracker");

        // load and unload memory storage
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> client.execute(MemoryBank::loadDefault));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(MemoryBank::unload));

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            // opening Chest Tracker GUI with no screen open
            if (client.screen == null && client.getOverlay() == null)
                while (OPEN_GUI.consumeClick()) {
                    openInGame(client, null);
                }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (Minecraft.getInstance().level == null) return;
            if (screen instanceof AbstractContainerScreen<?>) {
                // opening Chest Tracker GUI with a screen open
                ScreenKeyboardEvents.afterKeyPress(screen).register((parent, key, scancode, modifiers) -> {
                    // don't search in search bars, etc
                    if (ShouldIgnoreKey.EVENT.invoker().shouldIgnoreKey()) {
                        return;
                    }

                    if (OPEN_GUI.matches(key, scancode)) {
                        openInGame(client, parent);
                    }
                });

                // counting items after screen close
                ScreenEvents.remove(screen).register(screen1 -> {
                    if (MemoryBank.INSTANCE == null) return;
                    if (Minecraft.getInstance().level == null) return;
                    var loc = LocationTracking.popLocation();
                    if (loc == null) return;
                    var memory = GetMemory.EVENT.invoker().createMemory(loc, ((AbstractContainerScreen<?>) screen1), Minecraft.getInstance().level);
                    if (memory.hasValue()) {
                        if (MemoryBank.INSTANCE.getMetadata().getFilteringSettings().onlyRememberNamed && memory.get().name() == null) return;
                        MemoryBank.INSTANCE.addMemory(loc.key(), loc.pos(), memory.get());
                    }
                });
            }
        });

        LocationTracking.setup();
        MemoryIntegrity.setup();
        ImagePixelReader.setup();
        Storage.setup();
        DeveloperOverlay.setup();
        GuiApiDefaults.setup();

        ConnectionSettings.load();
    }
}
