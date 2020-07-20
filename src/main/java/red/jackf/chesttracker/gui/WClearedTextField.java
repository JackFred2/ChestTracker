package red.jackf.chesttracker.gui;

import spinnery.widget.WTextField;

public class WClearedTextField extends WTextField {

    @Override
    public void onMouseClicked(float mouseX, float mouseY, int mouseButton) {
        if (!this.active) this.setText("");
        super.onMouseClicked(mouseX, mouseY, mouseButton);
    }
}
