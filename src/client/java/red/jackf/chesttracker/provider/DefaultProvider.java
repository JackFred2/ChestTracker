package red.jackf.chesttracker.provider;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.gui.GetCustomName;
import red.jackf.chesttracker.api.provider.InteractionTracker;
import red.jackf.chesttracker.api.provider.MemoryBuilder;
import red.jackf.chesttracker.api.provider.Provider;
import red.jackf.chesttracker.api.provider.ProviderUtils;
import red.jackf.chesttracker.api.provider.def.DefaultMemoryCreator;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.jackfredlib.api.base.ResultHolder;
import red.jackf.jackfredlib.client.api.gps.Coordinate;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;

import java.util.List;
import java.util.Optional;

public class DefaultProvider implements Provider {
    @Override
    public ResourceLocation name() {
        return ChestTracker.id("default");
    }

    @Override
    public boolean applies(Coordinate coordinate) {
        return true;
    }

    @Override
    public Optional<Pair<ResourceLocation, BlockPos>> getKeyOverride(ClientBlockSource source) {
        if (source.blockState().is(Blocks.ENDER_CHEST))
            return Optional.of(Pair.of(MemoryBank.ENDER_CHEST_KEY, BlockPos.ZERO));
        return Optional.empty();
    }

    @Override
    public Optional<MemoryBuilder.Entry> createMemory(AbstractContainerScreen<?> screen) {
        var tracker = InteractionTracker.INSTANCE;
        if (tracker.getPlayerLevel().isPresent()) {
            ResultHolder<MemoryBuilder.Entry> result = DefaultMemoryCreator.EVENT.invoker().get(this, screen);
            if (result.hasValue()) return Optional.of(result.get());
        }
        return Optional.empty();
    }

    public static void setup() {
        // regular block tracking
        DefaultMemoryCreator.EVENT.register(EventPhases.FALLBACK_PHASE, (provider, screen) -> {
            if (InteractionTracker.INSTANCE.getLastBlockSource().isEmpty()) return ResultHolder.pass();
            ClientBlockSource source = InteractionTracker.INSTANCE.getLastBlockSource().get();

            @Nullable ResourceLocation currentKey = ProviderHandler.getCurrentKey();
            if (currentKey == null) return ResultHolder.pass();

            if (!ProviderUtils.defaultShouldRemember(source)) return ResultHolder.pass();

            List<BlockPos> connected = ConnectedBlocksGrabber.getConnected(source.level(), source.blockState(), source.pos());
            BlockPos rootPos = connected.get(0);

            List<ItemStack> items = ProviderUtils.getNonPlayerStacksAsList(screen);

            // get connected, minus the original pos
            return ResultHolder.value(MemoryBuilder.create(items)
                                                   .withCustomName(GetCustomName.EVENT.invoker().getName(source, screen).getNullable())
                                                   .inContainer(source.blockState().getBlock())
                                                   .otherPositions(connected.stream()
                                                                            .filter(pos -> !pos.equals(rootPos))
                                                                            .toList())
                                                   .toEntry(currentKey, rootPos)
            );
        });

        DefaultMemoryCreator.EVENT.register(EventPhases.DEFAULT_PHASE, (provider, screen) -> {
            if (InteractionTracker.INSTANCE.getLastBlockSource().isEmpty()) return ResultHolder.pass();
            ClientBlockSource source = InteractionTracker.INSTANCE.getLastBlockSource().get();

            if (!source.blockState().is(Blocks.ENDER_CHEST)) return ResultHolder.pass();

            List<ItemStack> items = ProviderUtils.getNonPlayerStacksAsList(screen);

            return ResultHolder.value(MemoryBuilder.create(items)
                                                   .inContainer(Blocks.ENDER_CHEST)
                                                   .toEntry(MemoryBank.ENDER_CHEST_KEY, BlockPos.ZERO)
            );
        });
    }
}
