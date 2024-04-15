package red.jackf.chesttracker.impl.gui.invbutton;

import red.jackf.chesttracker.impl.gui.invbutton.ui.InventoryButton;

/**
 * Applied to AbstractContainerScreen to get the menu positions, and to add the button with a back reference to the screen.
 */
public interface CTButtonScreenDuck {
    int chesttracker$getLeft();

    int chesttracker$getTop();

    int chesttracker$getWidth();

    int chesttracker$getHeight();

    void chesttracker$setButton(InventoryButton button);
}
