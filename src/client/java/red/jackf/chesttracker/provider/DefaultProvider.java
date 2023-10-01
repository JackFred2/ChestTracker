package red.jackf.chesttracker.provider;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.gui.GetCustomName;
import red.jackf.chesttracker.api.provider.memory.MemoryBuilder;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.provider.InteractionTracker;
import red.jackf.chesttracker.api.provider.memory.MemoryEntry;
import red.jackf.chesttracker.api.provider.Provider;
import red.jackf.chesttracker.api.provider.ProviderUtils;
import red.jackf.chesttracker.api.provider.def.DefaultMemoryCreator;
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
    public Optional<MemoryEntry> createMemory(AbstractContainerScreen<?> screen) {
        var tracker = InteractionTracker.INSTANCE;
        if (tracker.getPlayerLevel().isPresent() && tracker.getLastBlockSource().isPresent()) {
            ClientLevel level = tracker.getPlayerLevel().get();

            ResultHolder<MemoryEntry> result = DefaultMemoryCreator.EVENT.invoker().get(screen, level);
            if (result.hasValue()) return Optional.of(result.get());
        }
        return Optional.empty();
    }

    public static void setup() {
        // regular block tracking
        DefaultMemoryCreator.EVENT.register(EventPhases.FALLBACK_PHASE, (screen, level) -> {
            if (InteractionTracker.INSTANCE.getLastBlockSource().isEmpty()) return ResultHolder.pass();
            ClientBlockSource source = InteractionTracker.INSTANCE.getLastBlockSource().get();

            if (!ProviderUtils.defaultShouldRemember(source)) return ResultHolder.pass();

            List<BlockPos> connected = ConnectedBlocksGrabber.getConnected(level, source.blockState(), source.pos());
            BlockPos rootPos = connected.get(0);

            List<ItemStack> items = ProviderUtils.getNonPlayerStacks(screen);

            // get connected, minus the original pos
            return ResultHolder.value(new MemoryEntry(
                    level.dimension().location(),
                    rootPos,
                    new MemoryBuilder(items)
                            .name(GetCustomName.EVENT.invoker().getName(source, screen).getNullable())
                            .otherPositions(connected.stream().filter(pos -> !pos.equals(rootPos)).toList())
            ));
        });
    }
}