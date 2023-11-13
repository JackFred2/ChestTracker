package red.jackf.chesttracker.compat.mods.wthit;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import red.jackf.chesttracker.ChestTracker;

public class ChestTrackerWTHITPlugin implements IWailaPlugin {
    // call after WTHIT's default handler, so we can skip lookup if installed on server
    private static final int PRIORITY = 1550 + 25;

    protected static final ResourceLocation CONFIG_SHOW_ICON = ChestTracker.id("show_icon");
    protected static final ResourceLocation CONFIG_SHOW_KEY_AND_LOCATION = ChestTracker.id("show_key_and_location");

    @Override
    public void register(IRegistrar registrar) {
        //possiblyFixWTHITTransferAPICauldronFluidRaceCondition();
        registrar.addComponent(WTHITClientContentsPreview.INSTANCE, TooltipPosition.BODY, Block.class, PRIORITY);

        registrar.addConfig(CONFIG_SHOW_ICON, true);
        registrar.addConfig(CONFIG_SHOW_KEY_AND_LOCATION, false);
    }

    /*
    private static void possiblyFixWTHITTransferAPICauldronFluidRaceCondition() {
        try {
            // call <clinit> before world load
            // noinspection UnstableApiUsage
            CauldronFluidContent.getForBlock(Blocks.AIR);
        } catch (Throwable ignored) {}
    }*/
}
