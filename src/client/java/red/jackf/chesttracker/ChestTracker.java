package red.jackf.chesttracker;

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
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.events.AfterPlayerPlaceBlock;
import red.jackf.chesttracker.api.gui.ScreenBlacklist;
import red.jackf.chesttracker.api.provider.MemoryBuilder;
import red.jackf.chesttracker.api.provider.Provider;
import red.jackf.chesttracker.compat.mods.ShareEnderChestIntegration;
import red.jackf.chesttracker.compat.servers.hypixel.HypixelProvider;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.DeveloperOverlay;
import red.jackf.chesttracker.gui.GuiApiDefaults;
import red.jackf.chesttracker.gui.screen.ChestTrackerScreen;
import red.jackf.chesttracker.gui.util.ImagePixelReader;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.memory.MemoryIntegrity;
import red.jackf.chesttracker.provider.DefaultIcons;
import red.jackf.chesttracker.provider.DefaultProvider;
import red.jackf.chesttracker.provider.InteractionTrackerImpl;
import red.jackf.chesttracker.provider.ProviderHandler;
import red.jackf.chesttracker.rendering.NameRenderer;
import red.jackf.chesttracker.storage.ConnectionSettings;
import red.jackf.chesttracker.storage.Storage;
import red.jackf.chesttracker.util.CachedClientBlockSource;
import red.jackf.jackfredlib.client.api.gps.Coordinate;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;
import red.jackf.whereisit.client.api.events.ShouldIgnoreKey;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ChestTracker implements ClientModInitializer {
    public static final String ID = "chesttracker";

    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
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
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> client.execute(() -> {
            var coord = Coordinate.getCurrent();
            if (coord.isPresent()) {
                ProviderHandler.load(coord.get());
                MemoryBank.loadDefault(coord.get());
            } else {
                ProviderHandler.unload();
                MemoryBank.unload();
            }
        }));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            MemoryBank.unload();
            ProviderHandler.unload();
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            // opening Chest Tracker GUI with no screen open
            if (client.screen == null && client.getOverlay() == null)
                while (OPEN_GUI.consumeClick())
                    openInGame(client, null);
        });

        ClientTickEvents.START_WORLD_TICK.register(ignored -> {
            if (MemoryBank.INSTANCE != null) MemoryBank.INSTANCE.getMetadata().incrementLoadedTime();
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
                if (!ScreenBlacklist.isBlacklisted(screen.getClass()))
                    ScreenEvents.remove(screen).register(screen1 -> {
                        if (ProviderHandler.INSTANCE == null) return;
                        var bank = MemoryBank.INSTANCE;
                        if (bank == null) return;
                        if (Minecraft.getInstance().level == null) return;

                        Optional<MemoryBuilder.Entry> entry = ProviderHandler.INSTANCE.createMemory((AbstractContainerScreen<?>) screen1);

                        if (entry.isPresent()) {
                            bank.addMemory(entry.get());
                            InteractionTrackerImpl.INSTANCE.clear();
                        }
                    });
                else
                    LOGGER.debug("Blacklisted screen class, ignoring");
            }
        });

        // auto add placed blocks with data, such as shulker boxes
        // TODO after fabric#3367 is merged: replace with that
        AfterPlayerPlaceBlock.EVENT.register((clientLevel, pos, state, placementStack) -> {
            if (ProviderHandler.INSTANCE == null || MemoryBank.INSTANCE == null) return;

            if (!MemoryBank.INSTANCE.getMetadata().getFilteringSettings().autoAddPlacedBlocks.blockPredicate.test(state))
                return;

            if (ProviderHandler.INSTANCE.getKeyOverride(new CachedClientBlockSource(clientLevel, pos, state)).isPresent())
                return;

            var key = ProviderHandler.getCurrentKey();
            if (key == null) return;

            List<ItemStack> items = null;
            Component name = null;

            // check for items
            var beData = BlockItem.getBlockEntityData(placementStack);
            if (beData != null && beData.contains("Items", Tag.TAG_LIST)) {
                var loadedItems = NonNullList.withSize(27, ItemStack.EMPTY);
                ContainerHelper.loadAllItems(beData, loadedItems);
                if (!loadedItems.isEmpty()) items = loadedItems;
            }

            // check for names
            if (placementStack.hasCustomHoverName())
                name = placementStack.getHoverName();
            else if (beData != null && beData.contains("CustomName"))
                name = Component.Serializer.fromJson(beData.getString("CustomName"));

            if (items != null || name != null) {
                var connected = ConnectedBlocksGrabber.getConnected(clientLevel, state, pos);
                connected.forEach(connectedPos -> MemoryBank.INSTANCE.removeMemory(key, connectedPos));

                var rootPos = connected.get(0);

                var entry = MemoryBuilder.create(items == null ? Collections.emptyList() : items)
                        .withCustomName(name)
                        .otherPositions(connected.stream().filter(pos2 -> !pos2.equals(rootPos)).toList())
                        .inContainer(state.getBlock())
                        .toEntry(key, rootPos );

                MemoryBank.INSTANCE.addMemory(entry);
            }
        });

        NameRenderer.setup();
        InteractionTrackerImpl.setup();
        MemoryIntegrity.setup();
        ImagePixelReader.setup();
        Storage.setup();
        DeveloperOverlay.setup();
        GuiApiDefaults.setup();
        DefaultProvider.setup();
        DefaultIcons.setup();
        ShareEnderChestIntegration.setup();

        ConnectionSettings.load();

        // TODO move into self plugin
        Provider.register(new HypixelProvider());
    }
}
