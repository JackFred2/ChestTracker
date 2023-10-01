package red.jackf.chesttracker.api.provider;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import red.jackf.chesttracker.api.provider.memory.MemoryEntry;
import red.jackf.chesttracker.provider.ProviderHandler;
import red.jackf.jackfredlib.client.api.gps.Coordinate;

import java.util.Optional;

public interface Provider {
    ResourceLocation name();

    boolean applies(Coordinate coordinate);

    Optional<MemoryEntry> createMemory(AbstractContainerScreen<?> screen);

    static void register(Provider provider) {
        ProviderHandler.register(provider);
    }
}
