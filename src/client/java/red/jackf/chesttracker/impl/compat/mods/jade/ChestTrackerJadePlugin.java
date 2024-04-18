package red.jackf.chesttracker.impl.compat.mods.jade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import red.jackf.chesttracker.impl.ChestTracker;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;

public class ChestTrackerJadePlugin implements IWailaPlugin {
    protected static final ResourceLocation CONFIG_SHOW_TEXT = ChestTracker.id("show_mod_name");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(JadeClientContentsPreview.INSTANCE, Block.class);
        registration.markAsClientFeature(JadeClientContentsPreview.ID);
        registration.addConfig(CONFIG_SHOW_TEXT, false);
        registration.markAsClientFeature(CONFIG_SHOW_TEXT);
    }
}
