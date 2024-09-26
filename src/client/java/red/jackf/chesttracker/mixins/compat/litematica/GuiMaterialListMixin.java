package red.jackf.chesttracker.mixins.compat.litematica;

import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.gui.GuiMaterialList;
import fi.dy.masa.litematica.gui.widgets.WidgetListMaterialList;
import fi.dy.masa.litematica.gui.widgets.WidgetMaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetInfoIcon;
import fi.dy.masa.malilib.util.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.chesttracker.impl.compat.mods.litematica.ModIcon;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.criteria.builtin.AnyOfCriterion;
import red.jackf.whereisit.client.api.events.SearchInvoker;
import red.jackf.whereisit.client.api.events.SearchRequestPopulator;

/**
 * Adds a 'Search Missing' button to the top of the material list screen. Also adds an info button to the top right letting the user know
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
        if (!ChestTrackerConfig.INSTANCE.instance().compatibility.litematica.materialListSearchButtons) return;

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

    @Inject(method = "initGui", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/gui/GuiMaterialList;addWidget(Lfi/dy/masa/malilib/gui/widgets/WidgetBase;)Lfi/dy/masa/malilib/gui/widgets/WidgetBase;"))
    private void addCTInfo(CallbackInfo ci) {
        var config = ChestTrackerConfig.INSTANCE.instance().compatibility.litematica;

        if (config.anyEnabled()) {
            String yes = StringUtils.translate("gui.yes");
            String no = StringUtils.translate("gui.no");

            var args = new Object[] {
                    config.materialListSearchButtons ? yes : no,
                    config.countEnderChestMaterials ? yes : no,
                    config.countNearbyMaterials ? yes : no
            };

            this.addWidget(new WidgetInfoIcon(this.width - 36, 10, ModIcon.INSTANCE, "chesttracker.compatibility.litematica.info", args));
        }
    }

}
