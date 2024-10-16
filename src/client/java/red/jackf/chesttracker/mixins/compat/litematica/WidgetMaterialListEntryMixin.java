package red.jackf.chesttracker.mixins.compat.litematica;

import fi.dy.masa.litematica.gui.widgets.WidgetListMaterialList;
import fi.dy.masa.litematica.gui.widgets.WidgetMaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntrySortable;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.client.api.events.SearchInvoker;
import red.jackf.whereisit.client.api.events.SearchRequestPopulator;

/**
 * Adds a 'Search' button to each individual material entry on the material list.
 */
@Mixin(value = WidgetMaterialListEntry.class, remap = false)
public abstract class WidgetMaterialListEntryMixin extends WidgetListEntrySortable<MaterialListEntry> {
    private WidgetMaterialListEntryMixin(int x, int y, int width, int height, MaterialListEntry entry, int listIndex) {
        super(x, y, width, height, entry, listIndex);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void doInit(int x, int y, int width, int height, boolean isOdd, MaterialListBase materialList, MaterialListEntry entry, int listIndex, WidgetListMaterialList listWidget, CallbackInfo ci) {
        if (!ChestTrackerConfig.INSTANCE.instance().compatibility.litematica.materialListSearchButtons) return;

        int ignoreButtonWidth = StringUtils.getStringWidth(StringUtils.translate("litematica.gui.button.material_list.ignore"));

        if (entry != null) {
            ButtonGeneric searchButton = new ButtonGeneric(x + width - ignoreButtonWidth - 10, y + 1, -1, true, "Search");

            ItemStack stack = entry.getStack();

            this.addButton(searchButton, (buttonBase, i) -> {
                SearchRequest request = new SearchRequest();
                SearchRequestPopulator.addItemStack(request, stack, SearchRequestPopulator.Context.INVENTORY_PRECISE);
                SearchInvoker.doSearch(request);
            });
        }

    }
}
