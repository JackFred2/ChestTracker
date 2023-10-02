package red.jackf.chesttracker.compat.mods.shulkerboxtooltip;

import com.misterpemodder.shulkerboxtooltip.api.ShulkerBoxTooltipApi;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProviderRegistry;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;

public class ChestTrackerShulkerBoxTooltip implements ShulkerBoxTooltipApi {
    @Override
    public void registerProviders(@NotNull PreviewProviderRegistry registry) {
        if (ChestTrackerConfig.INSTANCE.instance().compatibility.shulkerBoxTooltipIntegration)
            registry.register(ChestTracker.id("client_ender_chest"), new ClientEnderChestPreviewProvider(), Items.ENDER_CHEST);
    }
}
