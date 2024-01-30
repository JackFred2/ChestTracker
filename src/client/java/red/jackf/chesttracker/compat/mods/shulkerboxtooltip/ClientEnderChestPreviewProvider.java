package red.jackf.chesttracker.compat.mods.shulkerboxtooltip;

import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.PreviewType;
import com.misterpemodder.shulkerboxtooltip.api.ShulkerBoxTooltipApi;
import com.misterpemodder.shulkerboxtooltip.api.color.ColorKey;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.memory.MemoryBank;

import java.util.Collections;
import java.util.List;

import static net.minecraft.network.chat.Component.translatable;

public class ClientEnderChestPreviewProvider implements PreviewProvider {
    @Override
    public boolean shouldDisplay(@NotNull PreviewContext context) {
        if (MemoryBank.INSTANCE == null) return false;
        var memories = MemoryBank.INSTANCE.getMemories(MemoryBank.ENDER_CHEST_KEY);
        return memories != null && !memories.isEmpty();
    }

    @Override
    public List<ItemStack> getInventory(@NotNull PreviewContext context) {
        if (MemoryBank.INSTANCE == null) return Collections.emptyList();
        return MemoryBank.INSTANCE.getCounts(MemoryBank.ENDER_CHEST_KEY, m -> true, MemoryBank.CountMergeMode.NEVER);
    }

    @Override
    public int getInventoryMaxSize(@NotNull PreviewContext context) {
        return 27;
    }

    @Override
    public int getPriority() {
        // overrides SBT's default handler
        return 1500;
    }

    @Override
    public boolean isFullPreviewAvailable(@NotNull PreviewContext context) {
        return true;
    }

    @Override
    public ColorKey getWindowColorKey(@NotNull PreviewContext context) {
        return ColorKey.ENDER_CHEST;
    }

    @Override
    public List<Component> addTooltip(@NotNull PreviewContext context) {
        if (ShulkerBoxTooltipApi.getCurrentPreviewType(true) == PreviewType.FULL)
            return List.of(
                    translatable("chesttracker.compatibility.brand", translatable("chesttracker.title").withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY)
            );
        else
            return Collections.emptyList();
    }
}
