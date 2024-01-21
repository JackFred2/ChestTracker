package red.jackf.chesttracker.gui.invbutton;

import red.jackf.chesttracker.gui.invbutton.ui.InventoryButton;

/**
 * Applied to AbstractContainerScreen to get the menu positions, and to add the button with a back reference to the screen.
 */
public interface CTScreenDuck {
    int chesttracker$getLeft();

    int chesttracker$getTop();

    int chesttracker$getWidth();

    int chesttracker$getHeight();

    void chesttracker$setButton(InventoryButton button);
}
