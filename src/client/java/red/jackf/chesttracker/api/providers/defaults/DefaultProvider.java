package red.jackf.chesttracker.api.providers.defaults;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.memory.Memory;
import red.jackf.chesttracker.api.memory.MemoryBankAccess;
import red.jackf.chesttracker.api.providers.*;
import red.jackf.chesttracker.api.providers.context.BlockPlacedContext;
import red.jackf.chesttracker.api.providers.context.ScreenCloseContext;
import red.jackf.chesttracker.api.providers.context.ScreenOpenContext;
import red.jackf.chesttracker.impl.ChestTracker;
import red.jackf.jackfredlib.api.base.ResultHolder;
import red.jackf.jackfredlib.client.api.gps.Coordinate;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    public ResourceLocation id() {
        return ChestTracker.id("default_provider");
    }

    @Override
    public boolean appliesTo(Coordinate coordinate) {
        return true;
    }

    @Override
    public void onConnect(Coordinate coordinate) {
        MemoryBankAccess.INSTANCE.loadWithDefaults(coordinate);
    }

    @Override
    public void onScreenOpen(ScreenOpenContext context) {
        InteractionTracker.INSTANCE.getLastBlockSource().flatMap(this::getMemoryLocation).ifPresent(context::setMemoryLocation);
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
        Optional<MemoryLocation> memoryLocation = this.getMemoryLocation(context.getBlockSource());
        if (memoryLocation.isEmpty() || memoryLocation.get().isOverride()) return;

        MemoryBankAccess.INSTANCE.getLoaded().ifPresent(bank -> {
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

            if (items != null || name != null) {
                List<BlockPos> connected = ConnectedBlocksGrabber.getConnected(
                        context.getBlockSource().level(),
                        context.getBlockSource().blockState(),
                        context.getBlockSource().pos()
                );

                connected.forEach(connectedPos -> bank.removeMemory(memoryLocation.get().memoryKey(), connectedPos));

                BlockPos rootPos = connected.get(0);

                Memory memory = MemoryBuilder.create(items == null ? Collections.emptyList() : items)
                        .withCustomName(name)
                        .otherPositions(connected.stream().filter(pos2 -> !pos2.equals(rootPos)).toList())
                        .inContainer(context.getBlockSource().blockState().getBlock())
                        .build();

                bank.addMemory(memoryLocation.get().memoryKey(), rootPos, memory);
            }
        });
    }

    @Override
    public Optional<MemoryLocation> getMemoryLocation(ClientBlockSource cbs) {
        return Optional.ofNullable(DefaultProviderMemoryLocation.EVENT.invoker().getOverride(cbs).getNullable());
    }
}
