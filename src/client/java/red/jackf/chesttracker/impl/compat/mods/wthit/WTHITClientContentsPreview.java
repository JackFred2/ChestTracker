package red.jackf.chesttracker.impl.compat.mods.wthit;

import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemListComponent;
import mcp.mobius.waila.api.data.ItemData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import red.jackf.chesttracker.api.memory.Memory;
import red.jackf.chesttracker.api.memory.MemoryBankAccess;
import red.jackf.chesttracker.api.providers.ProviderUtils;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.chesttracker.impl.util.ItemStacks;

import java.util.Optional;

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

        MemoryBankAccess.INSTANCE.getLoaded().ifPresent(bank -> {
            Optional<Memory> memory = bank.getMemory(ProviderUtils.getPlayersCurrentKey(), accessor.getPosition());
            if (memory.isEmpty()) return;

            // show items
            var stacks = ItemStacks.flattenStacks(memory.get().items(), true);
            tooltip.setLine(ItemData.ID, new ItemListComponent(stacks, config.getInt(ItemData.CONFIG_MAX_HEIGHT)));

            if (Screen.hasShiftDown() && config.getBoolean(ChestTrackerWTHITPlugin.CONFIG_SHOW_TEXT)) {
                tooltip.addLine(translatable("chesttracker.compatibility.brand", translatable("chesttracker.title").withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
            }

            // add name
            var formatter = IWailaConfig.get().getFormatter();
            if (tooltip.getLine(WailaConstants.OBJECT_NAME_TAG) == null && memory.get().name() != null) {
                tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, formatter.blockName(memory.get().name().getString()
                        + " ("
                        + accessor.getBlock().getName().getString()
                        + ")"));
            }
        });
    }
}
