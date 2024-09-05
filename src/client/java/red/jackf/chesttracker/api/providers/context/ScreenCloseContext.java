package red.jackf.chesttracker.api.providers.context;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.api.providers.ServerProvider;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Context relating to when a screen with item slots has closed.
 *
 * @see ServerProvider#onScreenClose(ScreenCloseContext)
 */
public interface ScreenCloseContext {
    /**
     * <p>Gets the screen that has closed. This is called <i>after</i> it has been {@link Screen#removed()}.</p>
     *
     * <p>Note that it's not recommended to use {@link Screen#getTitle()} for memory names, as custom names may be returned
     * from {@link red.jackf.chesttracker.api.gui.GetCustomName#EVENT}. Additionally, that method will return user-set
     * custom names if present, which shouldn't be stored as part of a memory. For a replacement, see {@link ScreenCloseContext#getCustomTitle()}.</p>
     *
     * @return Gets the screen that has closed.
     */
    AbstractContainerScreen<?> getScreen();

    /**
     * Gets the title of this screen. Usage of this method is preferred over {@link Screen#getTitle()}, as it will not return
     * any override set by the user.
     *
     * @return The title of this screen.
     */
    Component getTitle();

    /**
     * Returns the custom (i.e. anvil renamed) title of this screen. Usage of this method is preferred over {@link Screen#getTitle()} as it will not
     * return any override set by the user.
     *
     * @return The custom title of this screen, or an empty optional if no custom title is present.
     */
    Optional<Component> getCustomTitle();

    /**
     * Returns all non-{@link ItemStack#isEmpty()}, non-player inventory stacks in the slots of this screen.
     *
     * @return All non-empty ItemStacks in this screen.
     */
    List<ItemStack> getItems();

    /**
     * <p>Returns all item stacks, including {@link ItemStack#isEmpty()} stacks. You should generally prefer other methods
     * unless a full representation of the empty slots are useful, i.e. with ender chests.</p>
     *
     * <p>The list of items gets trimmed after the last non-empty slot in order to save space.</p>
     * @return All ItemStacks in this screen, including empty ones.
     */
    List<ItemStack> getItemsWithEmpty();

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
}
