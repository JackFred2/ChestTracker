package red.jackf.chesttracker.compat.mods.searchables;

import com.blamejared.searchables.api.SearchableComponent;
import com.blamejared.searchables.api.SearchableType;
import com.blamejared.searchables.api.autcomplete.AutoCompletingEditBox;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.widget.CustomEditBox;
import red.jackf.chesttracker.gui.widget.WidgetZOffsetWrapper;
import red.jackf.chesttracker.util.ItemStacks;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SearchablesUtil {
    public static final SearchableType<ItemStack> ITEM_STACK = buildType();

    private static SearchableType<ItemStack> buildType() {
        return new SearchableType.Builder<ItemStack>()
                .defaultComponent(SearchableComponent.create("text", ItemStacks::defaultPredicate))
                .component(SearchableComponent.create("name", SearchablesUtil::stackNameSuggestions, ItemStacks::namePredicate))
                .component(SearchableComponent.create("tooltip", ItemStacks::tooltipPredicate))
                .component(SearchableComponent.create("id", stack -> Optional.of(BuiltInRegistries.ITEM.getKey(stack.getItem())
                        .getPath())))
                .component(SearchableComponent.create("tag", ItemStacks::tagPredicate))
                .component(SearchableComponent.create("mod", stack -> Optional.of(BuiltInRegistries.ITEM.getKey(stack.getItem()))
                        .map(ResourceLocation::getNamespace)))
                .component(SearchableComponent.create("enchantment", ItemStacks::enchantmentPredicate))
                .component(SearchableComponent.create("potion", ItemStacks::potionOrEffectPredicate))
                .build();
    }

    private static Optional<String> stackNameSuggestions(ItemStack stack) {
        if (stack.hasCustomHoverName() || ChestTrackerConfig.INSTANCE.instance().gui.autocompleteShowsRegularNames)
            return Optional.of(stack.getHoverName().getString());
        return Optional.empty();
    }

    public static EditBox getEditBox(
            Font font,
            int x,
            int y,
            int width,
            int height,
            EditBox previous,
            Supplier<List<ItemStack>> itemSupplier,
            Consumer<String> callback) {
        AutoCompletingEditBox<ItemStack> box = new AutoCompletingEditBox<>(
                font,
                x,
                y,
                width,
                height,
                previous,
                CustomEditBox.SEARCH_MESSAGE,
                ITEM_STACK,
                itemSupplier
        );

        var formatter = SearchablesUtil.getFormatter();
        box.setFormatter(formatter);
        box.addResponder(formatter);

        box.addResponder(callback);

        return box;
    }

    public static CustomSearchablesFormatter getFormatter() {
        return new CustomSearchablesFormatter(ITEM_STACK);
    }

    public static boolean ifSearchables(EditBox box, Predicate<AbstractWidget> ifSearchablesBox) {
        if (box instanceof AutoCompletingEditBox<?> autoCompletingEditBox) {
            return ifSearchablesBox.test(autoCompletingEditBox.autoComplete());
        } else {
            return false;
        }
    }

    public static AbstractWidget getWrappedAutocomplete(EditBox search) {
        //noinspection unchecked
        return new WidgetZOffsetWrapper<>(((AutoCompletingEditBox<ItemStack>) search).autoComplete(), 250);
    }
}
