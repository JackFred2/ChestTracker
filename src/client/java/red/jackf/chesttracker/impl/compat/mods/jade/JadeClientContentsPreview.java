package red.jackf.chesttracker.impl.compat.mods.jade;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import red.jackf.chesttracker.api.memory.Memory;
import red.jackf.chesttracker.api.memory.MemoryBankAccess;
import red.jackf.chesttracker.impl.ChestTracker;
import red.jackf.chesttracker.impl.util.ItemStacks;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ScreenDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JadeClientContentsPreview implements IBlockComponentProvider {
    public static JadeClientContentsPreview INSTANCE = new JadeClientContentsPreview();
    private JadeClientContentsPreview() {}

    public static final ResourceLocation ID = ChestTracker.id("memory_preview");

    private static void possiblyAddItems(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config, Memory memory) {
        if (config.get(Identifiers.UNIVERSAL_ITEM_STORAGE) && accessor.getServerData().contains("JadeItemStorage"))
            return; // don't do it if jade is handling it
        if (config.get(Identifiers.MC_FURNACE)
                && accessor.getBlock() instanceof AbstractFurnaceBlock
                && accessor.getServerData()
                .contains("furnace", Tag.TAG_LIST))
            return; // don't do furnaces if handled so progress still shows

        var stacks = ItemStacks.flattenStacks(memory.items(), true);

        int max = config.getInt(accessor.showDetails() ? Identifiers.UNIVERSAL_ITEM_STORAGE_DETAILED_AMOUNT : Identifiers.UNIVERSAL_ITEM_STORAGE_NORMAL_AMOUNT);
        int perLine = config.getInt(Identifiers.UNIVERSAL_ITEM_STORAGE_ITEMS_PER_LINE);

        List<List<IElement>> lines = new ArrayList<>();
        List<IElement> currentLine = new ArrayList<>(perLine);
        for (int i = 0; i < max && i < stacks.size(); i++) {
            ItemStack item = stacks.get(i);
            currentLine.add(IElementHelper.get().item(item));
            if (currentLine.size() == perLine) {
                lines.add(currentLine);
                currentLine = new ArrayList<>(perLine);
            }
        }
        if (!currentLine.isEmpty()) lines.add(currentLine);
        for (int i = 0; i < lines.size(); i++) {
            tooltip.add(lines.get(i));
            if (i < lines.size() - 1) tooltip.setLineMargin(-1, ScreenDirection.DOWN, -1);
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        MemoryBankAccess.INSTANCE.getLoaded().ifPresent(bank -> {
            Optional<Memory> memory = bank.getMemory(accessor.getLevel(), accessor.getPosition());
            if (memory.isEmpty()) return;

            // items
            possiblyAddItems(tooltip, accessor, config, memory.get());

            Component name = memory.get().renderName();
            if (name != null) {
                tooltip.replace(Identifiers.CORE_OBJECT_NAME, IThemeHelper.get().title(name));
            }
        });
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
