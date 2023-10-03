package red.jackf.chesttracker.compat.servers.hypixel;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.chesttracker.api.provider.ProviderUtils;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class EnderChestReader {
    private static final Pattern TITLE = Pattern.compile("Ender Chest \\((?<current>\\d+)/\\d+\\)");

    private EnderChestReader() {}

    protected static boolean isEnderChest(AbstractContainerScreen<?> screen) {
        return screen.getTitle().getString().startsWith("Ender Chest");
    }

    protected static Optional<Integer> getPage(AbstractContainerScreen<?> screen) {
        var match = TITLE.matcher(screen.getTitle().getString());
        if (!match.find()) return Optional.empty();
        return Optional.of(Integer.parseInt(match.group("current")));
    }

    protected static List<ItemStack> getItems(AbstractContainerScreen<?> screen) {
        return ProviderUtils.getNonPlayerStacksAsStream(screen)
                .filter(stack -> !EnderChestReader.isMenuButton(stack))
                .toList();
    }

    private static boolean isMenuButton(ItemStack stack) {
        var name = stack.getHoverName().getString();
        return (stack.is(Items.BLACK_STAINED_GLASS_PANE) && name.isBlank())
                || (stack.is(Items.PLAYER_HEAD) && name.contains("Page"))
                || (stack.is(Items.ARROW) && name.contains("Back"))
                || (stack.is(Items.BARRIER) && name.contains("Close"));
    }
}
