package red.jackf.chesttracker.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
        return Language.getInstance().has(key) &&
                Language.getInstance().getOrDefault(key).toLowerCase().contains(filter);
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

    public static boolean tooltipPredicate(ItemStack stack, String filter) {
        var player = Minecraft.getInstance().player;
        if (player == null) return false;
        var advanced = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        for (Component line : stack.getTooltipLines(player, advanced)) {
            if (line.getString().toLowerCase().contains(filter)) return true;
        }
        return false;
    }

    public static boolean enchantmentPredicate(ItemStack stack, String filter) {
        var enchantments = EnchantmentHelper.getEnchantments(stack);
        if (enchantments.isEmpty()) return false;
        return enchantments.keySet().stream()
                           .anyMatch(ench -> {
                               if (testLang(ench.getDescriptionId(), filter)) return true;
                               var resloc = BuiltInRegistries.ENCHANTMENT.getKey(ench);
                               return resloc != null && resloc.toString().contains(filter);
                           });
    }

    public static boolean potionOrEffectPredicate(ItemStack stack, String filter) {
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
    public record LightweightStack(Item item, @Nullable CompoundTag tag) {
        public static final Codec<LightweightStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(LightweightStack::item),
                CompoundTag.CODEC.optionalFieldOf("tag").forGetter(stack -> Optional.ofNullable(stack.tag()))
        ).apply(instance, LightweightStack::new));

        public LightweightStack(ItemStack stack) {
            this(stack.getItem(), stack.getTag());
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private LightweightStack(Item item, Optional<CompoundTag> tag) {
            this(item, tag.orElse(null));
        }

        public ItemStack toStack() {
            var stack = new ItemStack(item);
            stack.setTag(tag);
            return stack;
        }
    }
}
