package red.jackf.chesttracker.api.providers.context;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import red.jackf.chesttracker.api.providers.MemoryLocation;

public interface ScreenOpenContext {
    AbstractContainerScreen<?> getScreen();

    void setTargetKeyAndPosition(MemoryLocation memoryLocation);
}
