package red.jackf.chesttracker.gui;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;

public class ChestTrackerScreen extends CottonClientScreen {
    private static final Component TITLE = Component.translatable("chesttracker.title");
    private final Screen parent;

    public ChestTrackerScreen(@Nullable Screen parent) {
        super(TITLE, new ChestTrackerGui());
        this.parent = parent;
        ChestTracker.LOGGER.debug("Open Screen");
    }

    public static class ChestTrackerGui extends LightweightGuiDescription {

        public ChestTrackerGui() {
            var root = new WGridPanel();
            setRootPanel(root);
            root.setSize(128, 120);
            root.setInsets(Insets.ROOT_PANEL);

            root.validate(this);
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
