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
import red.jackf.chesttracker.memory.Memory;
import red.jackf.chesttracker.memory.MemoryDatabase;
import red.jackf.chesttracker.memory.MemoryUtils;

import java.util.List;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
@Mixin(ShulkerBoxBlock.class)
public abstract class MixinShulkerBoxBlock {
    @Inject(method = "onPlaced", at = @At("HEAD"))
    public void chestTracker$onShulkerPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo ci) {
        if (world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            MemoryDatabase db = MemoryDatabase.getCurrent();
            CompoundTag tag = stack.getSubTag("BlockEntityTag");
            if (db != null && be instanceof ShulkerBoxBlockEntity && tag != null && tag.contains("Items", 9)) {
                DefaultedList<ItemStack> items = DefaultedList.ofSize(((ShulkerBoxBlockEntity) be).size(), ItemStack.EMPTY);
                Inventories.fromTag(tag, items);
                List<ItemStack> valid = MemoryUtils.condenseItems(items.stream().filter(stack2 -> !stack2.isEmpty()).collect(Collectors.toList()));
                db.mergeItems(world.getRegistryKey().getValue(), Memory.of(pos, valid, stack.hasCustomName() ? stack.getName() : null, null));
            }
        }
    }
}
