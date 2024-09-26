package red.jackf.chesttracker.mixins.compat.litematica;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.materials.MaterialListUtils;
import fi.dy.masa.malilib.util.ItemType;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import red.jackf.chesttracker.api.memory.CommonKeys;
import red.jackf.chesttracker.api.memory.MemoryBankAccess;
import red.jackf.chesttracker.api.memory.counting.CountingPredicate;
import red.jackf.chesttracker.api.memory.counting.StackMergeMode;
import red.jackf.chesttracker.api.providers.ProviderUtils;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;

/**
 * Adds the ender chest memories or the nearby container memories to the item count.
 */
@Mixin(MaterialListUtils.class)
public abstract class MaterialListUtilsMixin {

    @WrapOperation(method = {"updateAvailableCounts", "getMaterialList"},
            at = @At(value = "INVOKE",
                    target = "Lfi/dy/masa/litematica/materials/MaterialListUtils;getInventoryItemCounts(Lnet/minecraft/world/Container;)Lit/unimi/dsi/fastutil/objects/Object2IntOpenHashMap;"))
    private static Object2IntOpenHashMap<ItemType> addMemoriesToList(Container inventory, Operation<Object2IntOpenHashMap<ItemType>> original, @Local(argsOnly = true) Player player) {
        var inventoryStacks = original.call(inventory);
        var config = ChestTrackerConfig.INSTANCE.instance().compatibility.litematica;

        if (config.countEnderChestMaterials || config.countNearbyMaterials) {
            MemoryBankAccess.INSTANCE.getLoaded().ifPresent(bank -> {
                if (config.countEnderChestMaterials) {
                    for (ItemStack stack : bank.getCounts(CommonKeys.ENDER_CHEST_KEY, CountingPredicate.TRUE, StackMergeMode.ALL, true)) {
                        inventoryStacks.addTo(new ItemType(stack, true, false), stack.getCount());
                    }
                }

                if (config.countNearbyMaterials) {
                    ProviderUtils.getPlayersCurrentKey().ifPresent(currentKey -> {
                        for (ItemStack stack : bank.getCounts(currentKey, CountingPredicate.within(player.position(), 48), StackMergeMode.ALL, true)) {
                            inventoryStacks.addTo(new ItemType(stack, true, false), stack.getCount());
                        }
                    });
                }
            });
        }

        return inventoryStacks;
    }
}
