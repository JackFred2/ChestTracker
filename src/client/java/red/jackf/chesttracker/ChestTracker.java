package red.jackf.chesttracker;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.ChestTrackerScreen;
import red.jackf.chesttracker.gui.RenderUtil;
import red.jackf.chesttracker.memory.ItemMemory;
import red.jackf.chesttracker.memory.ScreenHandler;
import red.jackf.chesttracker.world.LocationTracking;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.client.api.SearchInvoker;
import red.jackf.whereisit.client.api.ShouldIgnoreKey;

import java.util.HashSet;
import java.util.Set;

public class ChestTracker implements ClientModInitializer {
    public static final String ID = "chesttracker";

    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
    }
    public static final Logger LOGGER = LogManager.getLogger();

    public static final KeyMapping OPEN_GUI = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.chesttracker.open_gui", InputConstants.Type.KEYSYM, InputConstants.KEY_GRAVE, "chesttracker.title")
    );

    @Override
    public void onInitializeClient() {
        ChestTrackerConfig.init();
        LOGGER.debug("Loading ChestTracker");

        LocationTracking.setup();

        // load and unload memory storage
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ItemMemory.load("test"));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ItemMemory.unload());

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            // opening Chest Tracker GUI with no screen open
            if (client.screen == null && client.getOverlay() == null)
                while (OPEN_GUI.consumeClick())
                    client.setScreen(new ChestTrackerScreen(null));
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?>) {
                // opening Chest Tracker GUI with a screen open
                ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                    // don't search in search bars, etc
                    if (ShouldIgnoreKey.EVENT.invoker().shouldIgnoreKey()) {
                        return;
                    }

                    if (OPEN_GUI.matches(key, scancode))
                        client.setScreen(new ChestTrackerScreen(screen1));
                });

                // counting items after screen close
                ScreenEvents.remove(screen).register(screen1 -> {
                    var loc = LocationTracking.popLocation();
                    if (loc == null) return;
                    ScreenHandler.handle(loc, (AbstractContainerScreen<?>) screen1);
                });
            }
        });

        // Title Colour grabber for the main GUI
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new RenderUtil.TitleListener());

        // add our memories as a handler for where is it
        SearchInvoker.EVENT.register((request, resultConsumer) -> {
            if (ItemMemory.INSTANCE == null) return true;
            var level = Minecraft.getInstance().level;
            var memoryId = level == null ? ChestTracker.id("unknown") : level.dimension().location();
            var thisDim = ItemMemory.INSTANCE.getMemories().get(memoryId);
            if (thisDim == null) return true;
            Set<SearchResult> results = new HashSet<>();
            for (var entry : thisDim.entrySet()) {
                for (var item : entry.getValue().items()) {
                    if (request.test(item)) {
                        results.add(new SearchResult(entry.getKey(), item));
                        break;
                    }
                }
            }
            if (!results.isEmpty()) resultConsumer.accept(results);
            return true;
        });
    }
}
