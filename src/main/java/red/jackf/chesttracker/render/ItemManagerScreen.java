package red.jackf.chesttracker.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;
import spinnery.client.screen.BaseScreen;
import spinnery.widget.WInterface;
import spinnery.widget.WPanel;
import spinnery.widget.api.Position;
import spinnery.widget.api.Size;

public class ItemManagerScreen extends BaseScreen {

    public ItemManagerScreen() {
        WInterface mainInterface = getInterface();

        int originalHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
        int originalWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();

        WPanel mainPanel = mainInterface.createChild(WPanel::new, Position.ORIGIN, Size.of(originalWidth, originalHeight))
                .setLabel(new TranslatableText("chesttracker.gui.title"));
    }
}
