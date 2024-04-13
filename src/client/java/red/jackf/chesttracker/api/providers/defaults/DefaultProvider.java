package red.jackf.chesttracker.api.providers.defaults;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.api.memory.Memory;
import red.jackf.chesttracker.api.memory.MemoryBankAccess;
import red.jackf.chesttracker.api.providers.BlockPlacedContext;
import red.jackf.chesttracker.api.providers.MemoryBuilder;
import red.jackf.chesttracker.api.providers.ScreenCloseContext;
import red.jackf.chesttracker.api.providers.ServerProvider;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;
import red.jackf.jackfredlib.api.base.ResultHolder;
import red.jackf.jackfredlib.client.api.gps.Coordinate;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;

import java.util.Collections;
import java.util.List;

/**
 * <p>The default provider used by Chest Tracker in normal worlds. This is loaded when no other provider is available.</p>
 */
public class DefaultProvider extends ServerProvider {
    /**
     * The default instance of this provider. This is free for use by other providers to delegate to if vanilla behavior
     * makes more sense at the time.
     */
    public static final DefaultProvider INSTANCE = new DefaultProvider();

    @Override
    public int getPriority() {
        return -100;
    }

    @Override
    public boolean appliesTo(Coordinate coordinate) {
        return true;
    }

    @Override
    public void onConnect(Coordinate coordinate) {
        MemoryBankAccess.INSTANCE.loadOrCreate(coordinate.id(), coordinate.userFriendlyName());
    }

    @Override
    public void onScreenClose(ScreenCloseContext context) {
        MemoryBankAccess.INSTANCE.getLoaded().ifPresent(bank -> {
            ResultHolder<DefaultProviderScreenClose.Result> memory = DefaultProviderScreenClose.EVENT.invoker().createMemory(this, context);

            if (memory.hasValue()) {
                bank.addMemory(memory.get().key(), memory.get().position(), memory.get().memory());
            }
        });
    }

    @Override
    public void onBlockPlaced(BlockPlacedContext context) {
        // if the block has an alternate mapping (i.e. ender chest) don't run
        if (this.getMemoryKeyOverride(context.getBlockSource()).isPresent())
            return;

        MemoryBankAccessImpl.ACCESS.getLoadedInternal().ifPresent(bank ->
                this.getPlayersCurrentKey(context.getBlockSource().level(), Minecraft.getInstance().player).ifPresent(currentKey -> {
            List<ItemStack> items = null;
            Component name = null;

            // check for items
            CompoundTag stackBeData = BlockItem.getBlockEntityData(context.getPlacementStack());
            if (stackBeData != null && stackBeData.contains("Items", Tag.TAG_LIST)) {
                var loadedItems = NonNullList.withSize(27, ItemStack.EMPTY);
                ContainerHelper.loadAllItems(stackBeData, loadedItems);
                if (!loadedItems.isEmpty()) items = loadedItems;
            }

            // check for names
            if (context.getPlacementStack().hasCustomHoverName())
                name = context.getPlacementStack().getHoverName();
            else if (stackBeData != null && stackBeData.contains("CustomName"))
                name = Component.Serializer.fromJson(stackBeData.getString("CustomName"));

            name = bank.getMetadata().getCompatibilitySettings().nameFilterMode.filter.apply(name);

            if (items != null || name != null) {
                List<BlockPos> connected = ConnectedBlocksGrabber.getConnected(
                        context.getBlockSource().level(),
                        context.getBlockSource().blockState(),
                        context.getBlockSource().pos()
                );
                connected.forEach(connectedPos -> bank.removeMemory(currentKey, connectedPos));

                BlockPos rootPos = connected.get(0);

                Memory memory = MemoryBuilder.create(items == null ? Collections.emptyList() : items)
                        .withCustomName(name)
                        .otherPositions(connected.stream().filter(pos2 -> !pos2.equals(rootPos)).toList())
                        .inContainer(context.getBlockSource().blockState().getBlock())
                        .build();

                bank.addMemory(currentKey, rootPos, memory);
            }
        }));
    }
}
