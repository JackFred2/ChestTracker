package red.jackf.chesttracker.impl.compat.servers.hypixel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.chesttracker.api.providers.context.ScreenCloseContext;
import red.jackf.chesttracker.mixins.AbstractContainerScreenAccessor;
import red.jackf.jackfredlib.client.api.gps.PlayerListSnapshot;
import red.jackf.jackfredlib.client.api.gps.ScoreboardSnapshot;

import java.util.List;
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

    static Optional<Integer> getSack(Component title) {
        assert Minecraft.getInstance().screen != null;
        Screen screen = Minecraft.getInstance().screen;
        if (!Minecraft.getInstance().screen.getTitle().getString().contains("Sack of Sacks")) {
            assert Minecraft.getInstance().player != null;
            if (((AbstractContainerScreenAccessor) screen).getLastClickSlot() != null) {
                return Optional.of(0);
            }
        }
        return Optional.empty();
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
        return context.getItemsMatching(stack -> !isMenuButton(stack));
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
                || (stack.is(Items.GRAY_DYE) && name.contains("Undiscovered"))
                || (stack.is(Items.ENDER_EYE) && name.contains("Filter"))
                || (stack.is(Items.BARRIER) && name.contains("Close"));
    }
}
