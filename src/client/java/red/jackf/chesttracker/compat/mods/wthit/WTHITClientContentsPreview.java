package red.jackf.chesttracker.compat.mods.wthit;

import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemListComponent;
import mcp.mobius.waila.api.data.ItemData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.memory.Memory;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.provider.ProviderHandler;
import red.jackf.chesttracker.util.CachedClientBlockSource;
import red.jackf.chesttracker.util.ItemStacks;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;

import static net.minecraft.network.chat.Component.translatable;

/**
 * Lookup the position in the current memory bank, and display if any are present
 */
public enum WTHITClientContentsPreview implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (!ChestTrackerConfig.INSTANCE.instance().compatibility.wthitIntegration) return;
        // dont do anything if existing
        if (tooltip.getLine(ItemData.ID) != null) return;

        Memory memory = MemoryBank.getMemoryAt(accessor.getWorld(), accessor.getPosition());
        if (memory == null) return;

        // show items
        var stacks = ItemStacks.flattenStacks(memory.items(), true);
        tooltip.setLine(ItemData.ID, new ItemListComponent(stacks, config.getInt(ItemData.CONFIG_MAX_HEIGHT)));

        if (Screen.hasShiftDown() && config.getBoolean(ChestTrackerWTHITPlugin.CONFIG_SHOW_TEXT)) {
            tooltip.addLine(translatable("chesttracker.compatibility.brand", translatable("chesttracker.title").withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
        }

        // add name
        var formatter = IWailaConfig.get().getFormatter();
        if (tooltip.getLine(WailaConstants.OBJECT_NAME_TAG) == null && memory.name() != null) {
            tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, formatter.blockName(memory.name().getString()
                    + " ("
                    + accessor.getBlock().getName().getString()
                    + ")"));
        }

        // debug lines
        if (ProviderHandler.INSTANCE != null && config.getBoolean(ChestTrackerWTHITPlugin.CONFIG_SHOW_KEY_AND_LOCATION)) {
            if (accessor.getWorld() instanceof ClientLevel clientLevel) {
                BlockState state = accessor.getBlockState();
                BlockPos pos = accessor.getPosition();
                ClientBlockSource cbs = new CachedClientBlockSource(clientLevel, pos, state);
                var override = ProviderHandler.INSTANCE.getKeyOverride(cbs);
                if (override.isPresent()) {
                    tooltip.addLine(getKeyAndLocationText(override.get().getFirst(), override.get().getSecond()));
                } else {
                    ResourceLocation key = ProviderHandler.getCurrentKey();
                    if (key == null) return;
                    BlockPos truePos = ConnectedBlocksGrabber.getConnected(clientLevel, state, pos).get(0);
                    tooltip.addLine(getKeyAndLocationText(key, truePos));
                }
            }
        }
    }

    private Component getKeyAndLocationText(ResourceLocation currentKey, BlockPos truePos) {
        return Component.literal("[" + truePos.toShortString() + "]").withStyle(ChatFormatting.AQUA)
                .append(Component.literal("@").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(currentKey.toString()).withStyle(ChatFormatting.GREEN));
    }
}
