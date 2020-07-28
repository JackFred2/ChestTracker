package red.jackf.chesttracker.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.chesttracker.tracker.LocationStorage;

import java.util.List;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
@Mixin(ShulkerBoxBlock.class)
public class MixinShulkerBoxBlock {
    @Inject(method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V",
        at = @At(value = "HEAD"))
    private void trackerLogPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo ci) {
        System.out.println("t");
        BlockEntity be = world.getBlockEntity(pos);
        LocationStorage storage = LocationStorage.get();
        if (storage != null) {
            CompoundTag tag = stack.getSubTag("BlockEntityTag");
            if (tag != null && tag.contains("Items", 9)) {
                DefaultedList<ItemStack> items = DefaultedList.ofSize(be != null ? ((ShulkerBoxBlockEntity) be).size() : 27, ItemStack.EMPTY);
                Inventories.fromTag(tag, items);
                List<ItemStack> valid = items.stream().filter(itemStack -> !itemStack.isEmpty()).collect(Collectors.toList());
                storage.mergeItems(pos, world, valid, stack.hasCustomName() ? stack.getName() : null, false);
            }
        }
    }
}
