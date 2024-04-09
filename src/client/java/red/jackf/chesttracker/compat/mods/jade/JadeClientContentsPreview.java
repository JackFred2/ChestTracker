package red.jackf.chesttracker.compat.mods.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.memory.Memory;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.util.ItemStacks;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Direction2D;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.network.chat.Component.translatable;

public enum JadeClientContentsPreview implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation ID = ChestTracker.id("memory_preview");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (config.get(Identifiers.UNIVERSAL_ITEM_STORAGE) && accessor.getServerData().contains("JadeItemStorage"))
            return; // don't do it if jade is handling it
        if (config.get(Identifiers.MC_FURNACE)
                && accessor.getBlock() instanceof AbstractFurnaceBlock
                && accessor.getServerData()
                .contains("furnace", Tag.TAG_LIST))
            return; // don't do furnaces if done

        Memory memory = MemoryBank.getMemoryAt(accessor.getLevel(), accessor.getPosition());
        if (memory == null) return;

        var stacks = ItemStacks.flattenStacks(memory.items(), true);

        int max = config.getInt(accessor.showDetails() ? Identifiers.MC_ITEM_STORAGE_DETAILED_AMOUNT : Identifiers.MC_ITEM_STORAGE_NORMAL_AMOUNT);
        int perLine = config.getInt(Identifiers.MC_ITEM_STORAGE_ITEMS_PER_LINE);

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
            if (i < lines.size() - 1) tooltip.setLineMargin(-1, Direction2D.DOWN, -1);
        }

        if (memory.name() != null) {
            tooltip.replace(Identifiers.CORE_OBJECT_NAME, IThemeHelper.get().title(memory.name()));
        }

        if (accessor.showDetails() && config.get(ChestTrackerJadePlugin.CONFIG_SHOW_TEXT)) {
            tooltip.add(
                    translatable("chesttracker.compatibility.brand", translatable("chesttracker.title").withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY)
            );
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
