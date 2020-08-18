package red.jackf.chesttracker.gui;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;

public class ItemListGui extends LightweightGuiDescription {

    public ItemListGui() {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);

        root.setSize(174, 200);

        root.validate(this);
    }
}
