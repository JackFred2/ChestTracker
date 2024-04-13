package red.jackf.chesttracker.api.providers;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import red.jackf.chesttracker.impl.ScreenCloseContextImpl;

import java.util.List;
import java.util.function.Predicate;

/**
 * Context relating to when a screen with item slots has closed.
 *
 * @see ServerProvider#onScreenClose(ScreenCloseContext)
 */
public interface ScreenCloseContext {
    /**
     * Gets the screen that has closed. This is called <i>after</i> it has been {@link Screen#removed()}.
     *
     * @return Gets the screen that has closed.
     */
    AbstractContainerScreen<?> getScreen();

    /**
     * Returns all non-{@link ItemStack#isEmpty()}, non-player inventory stacks in the slots of this screen.
     *
     * @return All non-empty ItemStacks in this screen.
     */
    List<ItemStack> getItems();

    /**
     * Returns all non-{@link ItemStack#isEmpty()}, non-player inventory stacks in the slots of this screen matching the given predicate.
     *
     * @return All non-empty ItemStacks in this screen matching the predicate.
     */
    List<ItemStack> getItemsMatching(Predicate<ItemStack> predicate);

    /**
     * Returns pairs of slots indexes to non-{@link ItemStack#isEmpty()} non-player inventory stacks in the given screen.
     *
     * @return A list of slots to non-empty ItemStacks in this screen.
     */
    List<Pair<Integer, ItemStack>> getItemsAndSlots();

    @ApiStatus.Internal
    static ScreenCloseContext createFor(AbstractContainerScreen<?> screen) {
        return new ScreenCloseContextImpl(screen);
    }
}
