package red.jackf.chesttracker.compat;

import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.OverlayDecider;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.RecipeScreen;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.impl.ItemEntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.gui.ItemListScreen;

import java.util.List;

import static red.jackf.chesttracker.ChestTracker.id;

@Environment(EnvType.CLIENT)
@SuppressWarnings("UnstableApiUsage")
public class REIPlugin implements REIPluginV0 {
    public static @NotNull ItemStack tryFindItem(double mouseX, double mouseY) {
        // Big List
        ItemStack item = tryFindInList(ContainerScreenOverlay.getEntryListWidget().children(), mouseX, mouseY);
        if (item != null) return item;

        // Favourites
        if (ContainerScreenOverlay.getFavoritesListWidget() != null) {
            item = tryFindInList(ContainerScreenOverlay.getFavoritesListWidget().children(), mouseX, mouseY);
            if (item != null) return item;
        }

        if (MinecraftClient.getInstance().currentScreen instanceof RecipeScreen) {
            item = tryFindInList((MinecraftClient.getInstance().currentScreen).children(), mouseX, mouseY);
            if (item != null) return item;
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

    @Override
    public Identifier getPluginIdentifier() {
        return id("rei_default");
    }

    @Override
    public void registerBounds(DisplayHelper displayHelper) {
        displayHelper.registerHandler(new OverlayDecider() {

            @Override
            public boolean isHandingScreen(Class<?> screenClass) {
                return false; // screenClass == ItemListScreen.class;
            }

            @Override
            public ActionResult shouldScreenBeOverlayed(Class<?> screen) {
                return ActionResult.SUCCESS;
            }
        });
    }
}
