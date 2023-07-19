package red.jackf.chesttracker.gui.util;

import com.blamejared.searchables.api.SearchableComponent;
import com.blamejared.searchables.api.SearchableType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.StringUtils;
import red.jackf.chesttracker.config.ChestTrackerConfig;

import java.util.Optional;

public class SearchablesUtil {
    public static final SearchableType<ItemStack> ITEM_STACK = buildType();

    private static SearchableType<ItemStack> buildType() {
        return new SearchableType.Builder<ItemStack>()
                .defaultComponent(SearchableComponent.create("text", SearchablesUtil::anyTextFilter))
                .component(SearchableComponent.create("name", SearchablesUtil::stackNameSuggestions, SearchablesUtil::stackNameFilter))
                .component(SearchableComponent.create("tooltip", SearchablesUtil::stackTooltipFilter))
                .component(SearchableComponent.create("id", stack -> Optional.of(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath())))
                .component(SearchableComponent.create("tag", SearchablesUtil::stackTagFilter))
                .component(SearchableComponent.create("mod", stack -> Optional.of(BuiltInRegistries.ITEM.getKey(stack.getItem())).map(ResourceLocation::getNamespace)))
                .component(SearchableComponent.create("enchantment", SearchablesUtil::stackEnchantmentFilter))
                .component(SearchableComponent.create("potion", SearchablesUtil::stackPotionFilter))
                .build();
    }

    private static boolean testLang(String key, String filter) {
        return Language.getInstance().has(key) &&
                Language.getInstance().getOrDefault(key).toLowerCase().contains(filter);
    }

    private static boolean anyTextFilter(ItemStack stack, String filter) {
        return (stackNameFilter(stack, filter)
                || stackLoreFilter(stack, filter)
                || stackEnchantmentFilter(stack, filter)
                || stackPotionFilter(stack, filter)
                || countFilter(stack, filter));
    }

    private static boolean countFilter(ItemStack stack, String filter) {
        try {
            if (filter.startsWith(">="))
                return stack.getCount() >= Integer.parseInt(filter.substring(2));
            else if (filter.startsWith(">"))
                return stack.getCount() > Integer.parseInt(filter.substring(1));
            else if (filter.startsWith("<="))
                return stack.getCount() <= Integer.parseInt(filter.substring(2));
            else if (filter.startsWith("<"))
                return stack.getCount() < Integer.parseInt(filter.substring(1));
            else if (filter.startsWith("="))
                return stack.getCount() == Integer.parseInt(filter.substring(1));
        } catch (NumberFormatException ignored) {}

        return false;
    }

    private static boolean stackLoreFilter(ItemStack stack, String filter) {
        var tag = stack.getTag();
        if (tag == null) return false;
        if (!tag.contains(ItemStack.TAG_DISPLAY, Tag.TAG_COMPOUND)) return false;
        var display = tag.getCompound(ItemStack.TAG_DISPLAY);
        if (!display.contains(ItemStack.TAG_LORE, Tag.TAG_LIST)) return false;
        var lore = tag.getList(ItemStack.TAG_LORE, Tag.TAG_STRING);
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.getString(i);
            try {
                var component = Component.Serializer.fromJson(line);
                if (component != null && component.getString().toLowerCase().contains(filter)) return true;
            } catch (Exception ex) {
                return false;
            }
        }
        return false;
    }

    private static boolean stackTooltipFilter(ItemStack stack, String filter) {
        var player = Minecraft.getInstance().player;
        if (player == null) return false;
        var advanced = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        for (Component line : stack.getTooltipLines(player, advanced)) {
            if (line.getString().toLowerCase().contains(filter)) return true;
        }
        return false;
    }

    private static boolean stackPotionFilter(ItemStack stack, String filter) {
        // potion presets
        var potion = PotionUtils.getPotion(stack);
        if (potion != Potions.EMPTY) {
            var langKey = potion.getName(stack.getDescriptionId() + ".effect.");
            if (testLang(langKey, filter)) return true;
            var resloc = BuiltInRegistries.POTION.getKey(potion);
            //noinspection ConstantValue
            if (resloc != null && resloc.toString().contains(filter)) return true;
        }

        // specific effects
        var effects = PotionUtils.getMobEffects(stack);
        for (MobEffectInstance effect : effects) {
            var langKey = effect.getDescriptionId();
            if (testLang(langKey, filter)) return true;
            var resloc = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect());
            if (resloc != null && resloc.toString().contains(filter)) return true;
        }

        return false;
    }

    private static boolean stackEnchantmentFilter(ItemStack stack, String filter) {
        var enchantments = EnchantmentHelper.getEnchantments(stack);
        if (enchantments.isEmpty()) return false;
        return enchantments.keySet().stream()
                .anyMatch(ench -> {
                    if (testLang(ench.getDescriptionId(), filter)) return true;
                    var resloc = BuiltInRegistries.ENCHANTMENT.getKey(ench);
                    return resloc != null && resloc.toString().contains(filter);
                });
    }

    private static boolean stackTagFilter(ItemStack stack, String filter) {
        return stack.getItemHolder().tags().anyMatch(tag -> tag.location().getPath().contains(filter));
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
