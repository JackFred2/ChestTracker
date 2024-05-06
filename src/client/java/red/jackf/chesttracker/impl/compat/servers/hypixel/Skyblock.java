package red.jackf.chesttracker.impl.compat.servers.hypixel;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.chesttracker.api.providers.context.ScreenCloseContext;
import red.jackf.jackfredlib.client.api.gps.PlayerListSnapshot;
import red.jackf.jackfredlib.client.api.gps.ScoreboardSnapshot;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

interface Skyblock {
    static boolean isPlayerOn() {
        return ScoreboardSnapshot.take().map(snapshot -> snapshot.title().getString().toLowerCase().startsWith("SKYBLOCK"))
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

    static List<ItemStack> getEnderChestItems(ScreenCloseContext context) {
        return context.getItemsMatching(stack -> !isMenuButton(stack));
    }

    private static boolean isMenuButton(ItemStack stack) {
        var name = stack.getHoverName().getString();
        return (stack.is(Items.BLACK_STAINED_GLASS_PANE) && name.isBlank())
                || (stack.is(Items.PLAYER_HEAD) && name.contains("Page"))
                || (stack.is(Items.ARROW) && name.contains("Back"))
                || (stack.is(Items.BARRIER) && name.contains("Close"));
    }
}
