package red.jackf.chesttracker.impl.compat.mods.shulkerboxtooltip;

import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.PreviewType;
import com.misterpemodder.shulkerboxtooltip.api.ShulkerBoxTooltipApi;
import com.misterpemodder.shulkerboxtooltip.api.color.ColorKey;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.api.memory.MemoryBank;
import red.jackf.chesttracker.api.memory.MemoryBankAccess;
import red.jackf.chesttracker.api.memory.MemoryKey;
import red.jackf.chesttracker.api.memory.counting.CountingPredicate;
import red.jackf.chesttracker.api.memory.counting.StackMergeMode;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;
import red.jackf.chesttracker.impl.memory.MemoryBankImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static net.minecraft.network.chat.Component.translatable;

public class ClientEnderChestPreviewProvider implements PreviewProvider {
    @Override
    public boolean shouldDisplay(@NotNull PreviewContext context) {
        Optional<MemoryBank> bank = MemoryBankAccess.INSTANCE.getLoaded();
        if (bank.isEmpty()) return false;

        Optional<MemoryKey> key = bank.get().getKey(MemoryBankImpl.ENDER_CHEST_KEY);
        return key.map(MemoryKey::isEmpty).orElse(false);
    }

    @Override
    public List<ItemStack> getInventory(@NotNull PreviewContext context) {
        Optional<MemoryBank> bank = MemoryBankAccess.INSTANCE.getLoaded();
        if (bank.isEmpty()) return Collections.emptyList();
        return bank.get().getCounts(MemoryBankImpl.ENDER_CHEST_KEY, CountingPredicate.TRUE, StackMergeMode.NEVER);
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
