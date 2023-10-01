package red.jackf.chesttracker.api.provider;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import red.jackf.jackfredlib.client.api.gps.Coordinate;

import java.util.Optional;

public interface Provider {
    boolean applies(Coordinate coordinate);

    Optional<MemoryEntry> createMemory(AbstractContainerScreen<?> screen);
}
