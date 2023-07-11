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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.ChestTrackerScreen;
import red.jackf.chesttracker.memory.ItemMemory;
import red.jackf.chesttracker.world.LocationTracking;
import red.jackf.whereisit.client.api.ShouldIgnoreKey;

import java.util.List;
import java.util.stream.Collectors;

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
        try {
            ChestTrackerConfig.INSTANCE.load();
            ChestTrackerConfig.INSTANCE.getConfig().validate();
        } catch (Exception ex) {
            LOGGER.error("Error loading Chest Tracker config, restoring default", ex);
        }
        ChestTrackerConfig.INSTANCE.save();
        LOGGER.debug("Loading ChestTracker");

        setupKeybinds();

        LocationTracking.setup();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?>)
                ScreenEvents.remove(screen).register(screen1 -> {
                    var loc = LocationTracking.popLocation();
                    if (loc == null) return;
                    ItemMemory.INSTANCE.addMemory(loc.level(), loc.pos(), getItems((AbstractContainerScreen<?>) screen1));
                });
        });
    }

    private List<ItemStack> getItems(AbstractContainerScreen<?> screen) {
        return screen.getMenu().slots.stream()
                .filter(slot -> !(slot.container instanceof Inventory) && slot.hasItem())
                .map(Slot::getItem)
                .collect(Collectors.toList());
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
