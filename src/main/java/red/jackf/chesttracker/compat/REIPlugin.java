package red.jackf.chesttracker.compat;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import me.shedaniel.rei.gui.VillagerRecipeViewingScreen;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.impl.ItemEntryStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static red.jackf.chesttracker.ChestTracker.id;

@SuppressWarnings("UnstableApiUsage")
public class REIPlugin implements REIPluginV0 {
    @Override
    public Identifier getPluginIdentifier() {
        return id("rei_default");
    }

    @Nullable
    public static ItemStack tryFindItem(double mouseX, double mouseY) {
        // Big List
        ItemStack item = tryFindInList(ContainerScreenOverlay.getEntryListWidget().children(), mouseX, mouseY);
        if (item != null) return item;

        // Favourites
        if (ContainerScreenOverlay.getFavoritesListWidget() != null) {
            item = tryFindInList(ContainerScreenOverlay.getFavoritesListWidget().children(), mouseX, mouseY);
            if (item != null) return item;
        }

        if (MinecraftClient.getInstance().currentScreen instanceof RecipeViewingScreen
                || MinecraftClient.getInstance().currentScreen instanceof VillagerRecipeViewingScreen) {
            item = tryFindInList((MinecraftClient.getInstance().currentScreen).children(), mouseX, mouseY);
            return item;
        }

        return ItemStack.EMPTY;
    }

    @Nullable
    private static ItemStack tryFindInList(@Nullable List<? extends Element> elements, double mouseX, double mouseY) {
        if (elements == null) return null;
        for (Element element : elements) {
            if (element instanceof Widget) {
                Widget widget = (Widget) element;
                if (widget instanceof EntryWidget && widget.containsMouse(mouseX, mouseY)) {
                    EntryWidget entryWidget = (EntryWidget) widget;
                    for (EntryStack entryStack : entryWidget.getEntries()) {
                        if (entryStack instanceof ItemEntryStack && !entryStack.getItemStack().isEmpty()) {
                            return entryStack.getItemStack();
                        }
                    }
                }
            }
        }
        return null;
    }
}
