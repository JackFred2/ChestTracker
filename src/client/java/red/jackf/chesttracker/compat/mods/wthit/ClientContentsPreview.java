package red.jackf.chesttracker.compat.mods.wthit;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.component.ItemListComponent;
import mcp.mobius.waila.api.data.ItemData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.provider.ProviderHandler;
import red.jackf.chesttracker.util.CachedClientBlockSource;
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

        if (ProviderHandler.INSTANCE == null) return;
        if (MemoryBank.INSTANCE == null) return;

        if (accessor.getWorld() instanceof ClientLevel clientLevel) {
            var blockSource = new CachedClientBlockSource(clientLevel, accessor.getPosition(), accessor.getBlockState());

            var override = ProviderHandler.INSTANCE.getKeyOverride(blockSource);

            ResourceLocation key;
            BlockPos pos;
            if (override.isPresent()) {
                key = override.get().getFirst();
                pos = override.get().getSecond();
            } else {
                key = ProviderHandler.getCurrentKey();
                pos = ConnectedBlocksGrabber.getConnected(accessor.getWorld(), accessor.getBlockState(), accessor.getPosition())
                        .get(0);
            }

            if (key == null || pos == null) return;

            var memoryKeys = MemoryBank.INSTANCE.getMemories(key);
            if (memoryKeys == null) return;
            var memory = memoryKeys.get(pos);
            if (memory == null) return;

            // show items
            var stacks = ItemStackUtil.flattenStacks(memory.items());
            if (config.getBoolean(ChestTrackerWTHITPlugin.CONFIG_SHOW_ICON))
                tooltip.setLine(ItemData.ID, new ItemListComponentWithChestTrackerIcon(stacks, config.getInt(ItemData.CONFIG_MAX_HEIGHT)));
            else
                tooltip.setLine(ItemData.ID, new ItemListComponent(stacks, config.getInt(ItemData.CONFIG_MAX_HEIGHT)));

            if (config.getBoolean(ChestTrackerWTHITPlugin.CONFIG_SHOW_KEY_AND_LOCATION))
                tooltip.addLine(getKeyAndLocationText(key, pos));
        }
    }

    private Component getKeyAndLocationText(ResourceLocation currentKey, BlockPos truePos) {
        return Component.literal("[" + truePos.toShortString() + "]").withStyle(ChatFormatting.AQUA)
                .append(Component.literal("@").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(currentKey.toString()).withStyle(ChatFormatting.GREEN));
    }
}
