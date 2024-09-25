package red.jackf.chesttracker.mixins.compat.litematica;

import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.gui.GuiMaterialList;
import fi.dy.masa.litematica.gui.widgets.WidgetListMaterialList;
import fi.dy.masa.litematica.gui.widgets.WidgetMaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.criteria.builtin.AnyOfCriterion;
import red.jackf.whereisit.client.api.events.SearchInvoker;
import red.jackf.whereisit.client.api.events.SearchRequestPopulator;

/**
 * Adds a 'Search Missing' button to the top of the material list screen.
 */
@Mixin(value = GuiMaterialList.class, remap = false)
public abstract class GuiMaterialListMixin extends GuiListBase<MaterialListEntry, WidgetMaterialListEntry, WidgetListMaterialList> {
    @Shadow @Final private MaterialListBase materialList;

    private GuiMaterialListMixin(int listX, int listY) {
        super(listX, listY);
    }

    // bad mixin @At ik
    @Inject(method = "initGui",
            at = @At(value = "INVOKE",
                    target = "Lfi/dy/masa/litematica/gui/GuiMaterialList;createButton(IIILfi/dy/masa/litematica/gui/GuiMaterialList$ButtonListener$Type;)I",
                    ordinal = 4,
                    shift = At.Shift.AFTER))
    private void addSearchAllButton(CallbackInfo ci, @Local(ordinal = 0) int x, @Local(ordinal = 1) int y) {
        x += StringUtils.getStringWidth(StringUtils.translate("litematica.gui.button.material_list.write_to_file")) + 10 + 1;

        ButtonGeneric searchButton = new ButtonGeneric(x, y, -1, 20,
                StringUtils.translate("chesttracker.compatibility.litematica.searchMissing"),
                StringUtils.translate("chesttracker.title"));

        this.addButton(searchButton, ((buttonBase, i) -> {
            SearchRequest request = new SearchRequest();
            AnyOfCriterion any = new AnyOfCriterion();

            for (MaterialListEntry missing : this.materialList.getMaterialsMissingOnly(true)) {
                SearchRequestPopulator.addItemStack(any, missing.getStack(), SearchRequestPopulator.Context.INVENTORY_PRECISE);
            }

            if (any.valid()) {
                request.accept(any.compact());
                SearchInvoker.doSearch(request);
            }
        }));
    }
}
