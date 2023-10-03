package red.jackf.chesttracker.compat.mods.wthit;

import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemListComponent;
import mcp.mobius.waila.api.data.ItemData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.provider.ProviderHandler;
import red.jackf.chesttracker.util.ItemStackUtil;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;

/**
 * Lookup the position in the current memory bank, and display if any are present
 */
public enum ClientContentsPreview implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (!ChestTrackerConfig.INSTANCE.instance().compatibility.wthitIntegration) return;
        // dont do anything if existing
        if (tooltip.getLine(ItemData.ID) != null) return;

        var currentKey = ProviderHandler.getCurrentKey();
        if (currentKey == null || MemoryBank.INSTANCE == null) return;
        var keyMemories = MemoryBank.INSTANCE.getMemories(currentKey);
        if (keyMemories == null) return;
        var truePos = ConnectedBlocksGrabber.getConnected(accessor.getWorld(), accessor.getBlockState(), accessor.getPosition()).get(0);
        var memory = keyMemories.get(truePos);
        if (memory == null || memory.items().isEmpty()) return;

        // show items
        var stacks = ItemStackUtil.flattenStacks(memory.items());
        if (config.getBoolean(ChestTrackerWTHITPlugin.CONFIG_SHOW_ICON))
            tooltip.setLine(ItemData.ID, new ItemListComponentWithChestTrackerIcon(stacks, config.getInt(ItemData.CONFIG_MAX_HEIGHT)));
        else
            tooltip.setLine(ItemData.ID, new ItemListComponent(stacks, config.getInt(ItemData.CONFIG_MAX_HEIGHT)));

        if (config.getBoolean(ChestTrackerWTHITPlugin.CONFIG_SHOW_KEY_AND_LOCATION))
            tooltip.addLine(getKeyAndLocationText(currentKey, truePos));
    }

    private Component getKeyAndLocationText(ResourceLocation currentKey, BlockPos truePos) {
        return Component.literal("[" + truePos.toShortString() + "]").withStyle(ChatFormatting.AQUA)
                .append(Component.literal("@").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(currentKey.toString()).withStyle(ChatFormatting.GREEN));
    }
}
