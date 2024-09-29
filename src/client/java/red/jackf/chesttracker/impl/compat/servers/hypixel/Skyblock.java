package red.jackf.chesttracker.impl.compat.servers.hypixel;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.chesttracker.api.providers.context.ScreenCloseContext;
import red.jackf.chesttracker.impl.util.ItemStacks;
import red.jackf.jackfredlib.client.api.gps.PlayerListSnapshot;
import red.jackf.jackfredlib.client.api.gps.ScoreboardSnapshot;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;


interface Skyblock {
    static boolean isPlayerOn() {
        return ScoreboardSnapshot.take().map(snapshot -> snapshot.title().getString().startsWith("SKYBLOCK"))
                .orElse(false);
    }

    static boolean isOnPrivateIsland() {
        var area = getArea();
        return area.isPresent() && area.get().equals("Private Island");
    }

    static Optional<String> getArea() {
        return PlayerListSnapshot.take().nameWithPrefixStripped("Area: ");
    }

    static Optional<Integer> getEnderChestPage(Component title) {
        var match = Pattern.compile("Ender Chest \\((?<current>\\d+)/\\d+\\)").matcher(title.getString());
        if (!match.find()) return Optional.empty();
        return Optional.of(Integer.parseInt(match.group("current")));
    }

    static Optional<Integer> getBackPackSlot(Component title) {
        var match = Pattern.compile("Backpack§r \\(Slot #(?<current>\\d+)\\)").matcher(title.getString());
        if (!match.find()) return Optional.empty();
        return Optional.of(Integer.parseInt(match.group("current")));
    }

    // Actual sacks (not the Sack of Sacks) of the same type always show the same title, regardless of size
    // i.e. 'Large Mining Sack' and 'Small Mining Sack' both have the title 'Mining Sack'
    // Combined with the fact that no item can be in more than one sack type, we can add use these as 'pages'.
    static BlockPos getFakePosForSackType(Component title) {
        return BlockPos.of(title.getString().hashCode());
    }

    static Optional<Integer> getPersonalVault(Component title) {
        if (!title.getString().equals("Personal Vault")) return Optional.empty();
        return Optional.of(0);
    }

    static List<ItemStack> getEnderChestItems(ScreenCloseContext context) {
        return context.getItemsMatching(stack -> !isMenuButton(stack));
    }

    static List<ItemStack> getBackPackItems(ScreenCloseContext context) {
        return context.getItemsMatching(stack -> !isMenuButton(stack));
    }

    static List<ItemStack> getSackItems(ScreenCloseContext context) {
        // set the size of the sack stack to the number of items contained for sorting purposes
        return context.getItemsMatching(stack -> !isMenuButton(stack)).stream()
                .map(sack -> {
                    Optional<Integer> count = getSizeOfSack(sack);

                    return count.map(sack::copyWithCount).orElse(null);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private static Optional<Integer> getSizeOfSack(ItemStack sack) {
        var pattern = Pattern.compile("Stored: (?<amount>\\d+)/.+");

        for (Component line : ItemStacks.getLore(sack)) {
            var match = pattern.matcher(line.getString().replace(",", ""));
            if (!match.find()) continue;

            int amount = Integer.parseInt(match.group("amount"));
            if (amount > 0) {
                return Optional.of(amount);
            } else {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    static List<ItemStack> getPersonalVaultItems(ScreenCloseContext context) {
        return context.getItemsMatching(stack -> !isMenuButton(stack));
    }

    private static boolean isMenuButton(ItemStack stack) {
        var name = stack.getHoverName().getString();
        return (stack.is(Items.BLACK_STAINED_GLASS_PANE) && name.isBlank())
                || (stack.is(Items.PLAYER_HEAD) && name.contains("Page"))
                || (stack.is(Items.ARROW) && name.contains("Back"))
                || (stack.is(Items.CAULDRON) && name.contains("Pickup"))
                || (stack.is(Items.CHEST) && name.contains("Insert"))
                || (stack.is(Items.GRAY_DYE))
                || (stack.is(Items.ENDER_EYE) && name.contains("Filter"))
                || (stack.is(Items.BARRIER) && name.contains("Close"));
    }
}
