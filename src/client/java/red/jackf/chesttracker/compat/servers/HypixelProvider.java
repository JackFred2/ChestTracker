package red.jackf.chesttracker.compat.servers;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.provider.MemoryBuilder;
import red.jackf.chesttracker.api.provider.Provider;
import red.jackf.jackfredlib.client.api.gps.Coordinate;

import java.util.Optional;

/**
 * Provider for Hypixel, mainly skyblock.
 */
public class HypixelProvider implements Provider {
    @Override
    public ResourceLocation name() {
        return ChestTracker.id("hypixel");
    }

    @Override
    public boolean applies(Coordinate coordinate) {
        return coordinate instanceof Coordinate.Multiplayer multiplayer && multiplayer.address().contains(".hypixel.net");
    }

    @Override
    public Optional<Pair<ResourceLocation, BlockPos>> getKeyOverride(ClientBlockSource source) {
        return Optional.empty();
    }

    @Override
    public Optional<MemoryBuilder.Entry> createMemory(AbstractContainerScreen<?> screen) {
        return Optional.empty();
    }
}
