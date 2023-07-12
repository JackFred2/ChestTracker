package red.jackf.chesttracker.gui;

import com.blamejared.searchables.api.SearchableComponent;
import com.blamejared.searchables.api.SearchableType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import red.jackf.chesttracker.config.ChestTrackerConfig;

import java.util.Optional;

public class SearchablesUtil {
    public static final SearchableType<ItemStack> ITEM_STACK = buildType();

    private static SearchableType<ItemStack> buildType() {
        return new SearchableType.Builder<ItemStack>()
                .defaultComponent(SearchableComponent.create("name", SearchablesUtil::stackNameSuggestions, SearchablesUtil::stackNameFilter))
                .component(SearchableComponent.create("id", stack -> Optional.of(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath())))
                .build();
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
