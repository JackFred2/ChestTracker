package red.jackf.chesttracker.impl.gui.invbutton;

import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.impl.gui.invbutton.ui.InventoryButton;
import red.jackf.chesttracker.impl.providers.ScreenOpenContextImpl;

/**
 * Applied to AbstractContainerScreen to get the menu positions, and to add the button with a back reference to the screen.
 */
public interface CTButtonScreenDuck {
    int chesttracker$getLeft();

    int chesttracker$getTop();

    int chesttracker$getWidth();

    int chesttracker$getHeight();

    void chesttracker$setButton(InventoryButton button);

    void chesttracker$setContext(ScreenOpenContextImpl openContext);

    @Nullable
    ScreenOpenContextImpl chesttracker$getContext();
}
