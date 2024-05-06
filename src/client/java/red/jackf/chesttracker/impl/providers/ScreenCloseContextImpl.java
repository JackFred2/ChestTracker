package red.jackf.chesttracker.impl.providers;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import red.jackf.chesttracker.api.gui.GetCustomName;
import red.jackf.chesttracker.api.providers.ProviderUtils;
import red.jackf.chesttracker.api.providers.context.ScreenCloseContext;
import red.jackf.chesttracker.impl.gui.util.CTTitleOverrideDuck;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public record ScreenCloseContextImpl(AbstractContainerScreen<?> screen) implements ScreenCloseContext {
    @ApiStatus.Internal
    public static ScreenCloseContext createFor(AbstractContainerScreen<?> screen) {
        return new ScreenCloseContextImpl(screen);
    }

    @Override
    public AbstractContainerScreen<?> getScreen() {
        return this.screen;
    }

    @Override
    public Component getTitle() {
        return GetCustomName.EVENT.invoker().getName(screen).asOptional().orElse(((CTTitleOverrideDuck) screen).chesttracker$getOriginalTitle());
    }

    @Override
    public Optional<Component> getCustomTitle() {
        return GetCustomName.EVENT.invoker().getName(screen).asOptional();
    }

    @Override
    public List<ItemStack> getItems() {
        return screen.getMenu().slots.stream()
                .filter(slot -> !ProviderUtils.isPlayerSlot(slot) && slot.hasItem())
                .map(Slot::getItem)
                .toList();
    }

    @Override
    public List<ItemStack> getItemsMatching(Predicate<ItemStack> predicate) {
        return this.getItems().stream().filter(stack -> !stack.isEmpty() && predicate.test(stack)).toList();
    }

    @Override
    public List<Pair<Integer, ItemStack>> getItemsAndSlots() {
        return screen.getMenu().slots.stream()
                .filter(slot -> !ProviderUtils.isPlayerSlot(slot) && slot.hasItem())
                .map(slot -> Pair.of(slot.index, slot.getItem()))
                .toList();
    }
}
