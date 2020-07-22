package red.jackf.chesttracker.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import spinnery.widget.WTextField;

@Environment(EnvType.CLIENT)
public class WClearedTextField extends WTextField {

    @Override
    public void onMouseClicked(float mouseX, float mouseY, int mouseButton) {
        if (!this.active) this.setText("");
        super.onMouseClicked(mouseX, mouseY, mouseButton);
    }
}
