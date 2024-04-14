package red.jackf.chesttracker.api.providers;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.providers.defaults.DefaultIcons;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;
import red.jackf.chesttracker.impl.providers.ProviderHandler;

import java.util.List;
import java.util.Optional;

/**
 * Utilities for working with the currently loaded provider.
 */
public interface ProviderUtils {

    /**
     * Returns the key that the player is currently in, according to the provider. This is usually the same as the current
     * level's dimension (minecraft:the_nether), but can be different or empty on custom providers
     * .
     * @return The player's current memory key.
     */
    static Optional<ResourceLocation> getPlayersCurrentKey() {
        return ProviderHandler.INSTANCE.getCurrentProvider().flatMap(provider -> {
            var level = Minecraft.getInstance().level;
            var player = Minecraft.getInstance().player;
            if (level == null || player == null) return Optional.empty();
            return provider.getPlayersCurrentKey(level, player);
        });
    }

    /**
     * Returns the currently loaded provider, if one is present.
     *
     * @return The current provider, or an empty optional if one isn't loaded (usually when not in-game).
     */
    static Optional<ServerProvider> getCurrentProvider() {
        return ProviderHandler.INSTANCE.getCurrentProvider();
    }

    /**
     * Returns the list of icons used by the default provider. May be used to augment a custom provider with the defaults.
     *
     * @return A list of standard icons used by the default provider.
     */
    static List<MemoryKeyIcon> getDefaultIcons() {
        return DefaultIcons.getDefaultIcons();
    }

    /**
     * Test whether, given the current memory bank settings, the given block source would be remembered by the default
     * provider.
     *
     * @param cbs Block source to test against.
     * @return Whether the default provider would remember the given block source.
     */
    static boolean defaultShouldRemember(ClientBlockSource cbs) {
        return MemoryBankAccessImpl.ACCESS.getLoadedInternal().map(bank -> bank.getMetadata()
                    .getFilteringSettings()
                    .rememberedContainers
                    .predicate
                    .test(cbs.blockState()))
                .orElse(false);
    }

    /**
     * Utility method for {@link ServerProvider#onScreenClose(ScreenCloseContext)}; tests whether a given slot is part of
     * a player's inventory (and usually ignored).
     *
     * @param slot Slot to test
     * @return Whether the given slot is backed by the player's inventory.
     */
    static boolean isPlayerSlot(Slot slot) {
        return slot.container instanceof Inventory;
    }
}
