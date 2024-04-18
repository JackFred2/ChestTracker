package red.jackf.chesttracker.impl.memory;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.api.memory.CommonKeys;
import red.jackf.chesttracker.impl.ChestTracker;
import red.jackf.chesttracker.api.memory.Memory;
import red.jackf.chesttracker.api.providers.ProviderUtils;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.chesttracker.impl.events.AfterPlayerDestroyBlock;
import red.jackf.chesttracker.impl.memory.metadata.IntegritySettings;
import red.jackf.jackfredlib.api.base.Memoizer;
import red.jackf.jackfredlib.client.api.toasts.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Tries to keep memories valid, by tracking block breaking and timing out old memories
 */
public class MemoryIntegrity {
    private static final Logger LOGGER = ChestTracker.getLogger("Integrity");
    private static final int TICKS_BETWEEN_ENTRY_REFILL = 600;
    private static final double PERIODIC_CHECK_RANGE_SQUARED = 32 * 32;
    private static final List<Map.Entry<BlockPos, Memory>> currentEntryList = new ArrayList<>();
    private static long lastEntryCheckCompleteTick = -1L;
    private static int currentEntryKeyIndex = 0;
    private static ResourceLocation currentMemoryKeyId = CommonKeys.OVERWORLD;
    private static int lastEntryListIndex = 0;
    private static final Supplier<CustomToast> toast = Memoizer.of(() ->
            ToastBuilder.builder(ToastFormat.WHITE, Component.translatable("chesttracker.gui.editMemoryBank.integrity"))
                    .withIcon(ToastIcon.modIcon(ChestTracker.ID))
                    .addMessage(Component.literal("Initial"))
                    .expiresWhenProgressComplete(2500L)
                    .progressPuller(toast1 -> {
                        if (toast1.getProgress() >= 1f) return Optional.empty();
                        if (currentEntryList.isEmpty()) return Optional.empty();
                        return Optional.of(Mth.clamp((float) lastEntryListIndex / currentEntryList.size(), 0, 1));
                    }).build());

    private MemoryIntegrity() {
    }

    public static void setup() {
        AfterPlayerDestroyBlock.EVENT.register(cbs -> MemoryBankAccessImpl.INSTANCE.getLoadedInternal().ifPresent(bank -> {
            // Called when a player breaks a block, to remove memories that would be contained there
            if (bank.getMetadata().getIntegritySettings().removeOnPlayerBlockBreak) {
                ProviderUtils.getPlayersCurrentKey().ifPresent(currentKey -> {
                    bank.removeMemory(currentKey, cbs.pos());
                    LOGGER.debug("Player Destroy Block: Removing {}@{}", cbs.pos().toShortString(), currentKey);
                });
            }
        }));

        ClientTickEvents.END_WORLD_TICK.register(level -> {
            MemoryBankImpl memoryBank = MemoryBankAccessImpl.INSTANCE.getLoadedInternal().orElse(null);

            if (memoryBank == null) {
                lastEntryCheckCompleteTick = -1;
                currentEntryList.clear();
                return;
            }

            IntegritySettings integrity = memoryBank.getMetadata().getIntegritySettings();

            // populate iteration list
            if (currentEntryList.isEmpty() && level.getGameTime() >= lastEntryCheckCompleteTick + TICKS_BETWEEN_ENTRY_REFILL) {
                ArrayList<ResourceLocation> keys = new ArrayList<>(memoryBank.getMemoryKeys());

                if (keys.isEmpty()) return;
                if (currentEntryKeyIndex >= keys.size()) currentEntryKeyIndex = 0;
                currentMemoryKeyId = keys.get(currentEntryKeyIndex++);
                Optional<MemoryKeyImpl> currentKey = memoryBank.getKeyInternal(currentMemoryKeyId);

                if (currentKey.isPresent() && !currentKey.get().isEmpty()) {
                    LOGGER.debug("Refreshing entry list <{}> @ {}", currentMemoryKeyId, level.getGameTime());
                    currentEntryList.addAll(currentKey.get().getMemories().entrySet());
                    if (ChestTrackerConfig.INSTANCE.instance().debug.showDevHud) {
                        toast.get().setTitle(Component.literal("Integrity: " + currentMemoryKeyId));
                        toast.get().setProgress(0f);
                        Toasts.INSTANCE.send(toast.get());
                    }

                    lastEntryListIndex = 0;
                }
            }

            // if no keys don't bother
            if (currentEntryList.isEmpty()) return;

            // mark as finished this key; wait for next
            if (lastEntryListIndex >= currentEntryList.size() || !memoryBank.getKeys().contains(currentMemoryKeyId)) {
                LOGGER.debug("Done checking <{}> @ {}", currentMemoryKeyId, level.getGameTime());
                if (ChestTrackerConfig.INSTANCE.instance().debug.showDevHud) {
                    toast.get().setProgress(1f);
                    toast.get().setMessage(List.of(
                            Component.literal("Complete")
                    ));
                }
                lastEntryCheckCompleteTick = level.getGameTime();
                currentEntryList.clear();
                return;
            }

            Map.Entry<BlockPos, Memory> currentEntry = currentEntryList.get(lastEntryListIndex++);
            if (ChestTrackerConfig.INSTANCE.instance().debug.showDevHud)
                toast.get().setMessage(List.of(Component.literal("Checking " + lastEntryListIndex + "/" + currentEntryList.size())));
            BlockPos currentPos = currentEntry.getKey();
            Memory currentMemory = currentEntry.getValue();

            // check if time has expired
            // exempt named from the check
            if (!integrity.preserveNamed || !currentMemory.hasCustomName()) {
                final Long expirySeconds = integrity.memoryLifetime.seconds;
                if (expirySeconds != null) {
                    final long secondsPastExpiry = switch (integrity.lifetimeCountMode) {
                        case REAL_TIME -> Duration.between(currentMemory.realTimestamp(), Instant.now()).toSeconds();
                        case WORLD_TIME -> (level.getGameTime() - currentMemory
                                .inGameTimestamp()) / SharedConstants.TICKS_PER_SECOND;
                        case LOADED_TIME -> (memoryBank.getMetadata().getLoadedTime() - currentMemory
                                .loadedTimestamp()) / SharedConstants.TICKS_PER_SECOND;
                    } - expirySeconds;

                    if (secondsPastExpiry > 0) {
                        memoryBank.removeMemory(currentMemoryKeyId, currentPos);
                        LOGGER.debug("Expiry: Removing {}@{}, {} seconds out of date", currentPos, currentMemoryKeyId, secondsPastExpiry);
                        return;
                    }
                }
            }

            // check if block is valid
            if (integrity.checkPeriodicallyForMissingBlocks) {
                LocalPlayer player = Minecraft.getInstance().player;
                ResourceLocation playerCurrentKey = ProviderUtils.getPlayersCurrentKey().orElse(null);
                if (player != null
                        && playerCurrentKey != null
                        && playerCurrentKey.equals(currentMemoryKeyId)
                        && level.isLoaded(currentPos)
                        && currentPos.distSqr(player.blockPosition()) < PERIODIC_CHECK_RANGE_SQUARED) {
                    if (!(level.getBlockEntity(currentPos) instanceof MenuProvider)) {
                        memoryBank.removeMemory(playerCurrentKey, currentPos);
                        LOGGER.debug("Periodic Check: Removing {}@{}", currentPos, currentMemoryKeyId);
                    }
                }
            }
        });
    }
}
