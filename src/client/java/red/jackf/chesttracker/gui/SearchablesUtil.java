package red.jackf.chesttracker.gui;

import com.blamejared.searchables.api.SearchableComponent;
import com.blamejared.searchables.api.SearchableType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.StringUtils;
import red.jackf.chesttracker.config.ChestTrackerConfig;

import java.util.Objects;
import java.util.Optional;

public class SearchablesUtil {
    public static final SearchableType<ItemStack> ITEM_STACK = buildType();

    private static SearchableType<ItemStack> buildType() {
        return new SearchableType.Builder<ItemStack>()
                .defaultComponent(SearchableComponent.create("text", SearchablesUtil::anyTextFilter))
                .component(SearchableComponent.create("name", SearchablesUtil::stackNameSuggestions, SearchablesUtil::stackNameFilter))
                .component(SearchableComponent.create("id", stack -> Optional.of(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath())))
                .component(SearchableComponent.create("tag", SearchablesUtil::stackTagFilter))
                .component(SearchableComponent.create("mod", stack -> Optional.of(BuiltInRegistries.ITEM.getKey(stack.getItem())).map(ResourceLocation::getNamespace)))
                .component(SearchableComponent.create("enchantment", SearchablesUtil::stackEnchantmentFilter))
                .build();
    }

    private static boolean stackEnchantmentFilter(ItemStack stack, String filter) {
        var enchantments = EnchantmentHelper.getEnchantments(stack);
        if (enchantments.isEmpty()) return false;
        return enchantments.keySet().stream()
                .map(BuiltInRegistries.ENCHANTMENT::getKey)
                .filter(Objects::nonNull)
                .anyMatch(resLoc -> resLoc.getPath().contains(filter));
    }

    private static boolean stackTagFilter(ItemStack stack, String filter) {
        return stack.getItemHolder().tags().anyMatch(tag -> tag.location().getPath().contains(filter));
    }

    private static boolean anyTextFilter(ItemStack stack, String filter) {
        return stackNameFilter(stack, filter);
    }

    private static Optional<String> stackNameSuggestions(ItemStack stack) {
        if (stack.hasCustomHoverName() || ChestTrackerConfig.INSTANCE.getConfig().gui.autocompleteShowsRegularNames)
            return Optional.of(stack.getHoverName().getString());
        return Optional.empty();
    }

    private static boolean stackNameFilter(ItemStack stack, String filter) {
        return StringUtils.containsIgnoreCase(stack.getHoverName().getString(), filter);
    }
}
