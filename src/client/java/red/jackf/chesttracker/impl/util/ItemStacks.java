package red.jackf.chesttracker.impl.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ItemStacks {
    /**
     * Combine and sort a list of ItemStacks in descending order of count
     */
    public static List<ItemStack> flattenStacks(List<ItemStack> in, boolean sortDescending) {
        var counts = new HashMap<LightweightStack, Integer>();
        for (ItemStack itemStack : in) {
            counts.merge(new LightweightStack(itemStack), itemStack.getCount(), Integer::sum);
        }
        var stream = counts.entrySet().stream()
                .map(entry -> {
                    var stack = entry.getKey().toStack();
                    stack.setCount(entry.getValue());
                    return stack;
                });
        if (sortDescending) stream = stream.sorted(Comparator.comparingInt(ItemStack::getCount).reversed());
        return stream.toList();
    }

    private static boolean testLang(String key, String filter) {
        return Language.getInstance().has(key) && Language.getInstance().getOrDefault(key).toLowerCase().contains(filter);
    }

    private static boolean testHolder(Holder<?> holder, String filter) {
        return holder.unwrapKey().map(key -> key.location().toString().toLowerCase().contains(filter)).orElse(false);
    }

    public static boolean defaultPredicate(ItemStack stack, String filter) {
        return (namePredicate(stack, filter)
                || tagPredicate(stack, filter)
                || lorePredicate(stack, filter)
                || tooltipPredicate(stack, filter)
                || enchantmentPredicate(stack, filter)
                || potionOrEffectPredicate(stack, filter)
                || countPredicate(stack, filter));
    }

    public static boolean namePredicate(ItemStack stack, String filter) {
        return StringUtils.containsIgnoreCase(stack.getHoverName().getString(), filter);
    }

    public static boolean tagPredicate(ItemStack stack, String filter) {
        return stack.getItemHolder().tags().anyMatch(tag -> tag.location().getPath().contains(filter));
    }

    private static boolean lorePredicate(ItemStack stack, String filter) {
        ItemLore lore = stack.get(DataComponents.LORE);

        if (lore == null) return false;

        for (Component line : lore.lines()) {
            String str = line.getString().toLowerCase();

            if (str.contains(filter)) return true;
        }

        return false;
    }

    public static boolean tooltipPredicate(ItemStack stack, String filter) {
        TooltipFlag flag = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        for (Component line : stack.getTooltipLines(Item.TooltipContext.of(Minecraft.getInstance().level), Minecraft.getInstance().player, flag)) {
            if (line.getString().toLowerCase().contains(filter)) return true;
        }
        return false;
    }

    public static boolean enchantmentPredicate(ItemStack stack, String filter) {
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        if (enchantments.isEmpty()) return false;

        return enchantments.keySet().stream()
                .anyMatch(ench -> {
                    if (ench.value().description().getString().toLowerCase().contains(filter)) return true;
                    return testHolder(ench, filter);
                });
    }

    public static boolean potionOrEffectPredicate(ItemStack stack, String filter) {
        // potion presets
        PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) return false;

        if (potionContents.potion().isPresent()) {
            String langKey = Potion.getName(potionContents.potion(), stack.getDescriptionId() + ".effect.");
            if (testLang(langKey, filter)) return true;
            ResourceLocation resloc = BuiltInRegistries.POTION.getKey(potionContents.potion().get().value());
            if (resloc != null && resloc.toString().contains(filter)) return true;
        }

        // specific effects
        var effects = potionContents.customEffects();
        for (MobEffectInstance effect : effects) {
            String langKey = effect.getDescriptionId();
            if (testLang(langKey, filter)) return true;
            ResourceLocation resloc = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
            if (resloc != null && resloc.toString().contains(filter)) return true;
        }

        return false;
    }

    private static boolean countPredicate(ItemStack stack, String filter) {
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
        } catch (NumberFormatException ignored) {
        }

        return false;
    }

    /**
     * Fake itemstack that has no count, and so easier equality check
     */
    private record LightweightStack(Item item, DataComponentPatch patch) {
        public LightweightStack(ItemStack stack) {
            this(stack.getItem(), stack.getComponentsPatch());
        }

        public ItemStack toStack() {
            ItemStack stack = new ItemStack(item);

            stack.applyComponents(patch);

            return stack;
        }
    }
}
