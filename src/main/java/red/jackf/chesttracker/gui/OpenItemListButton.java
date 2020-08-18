package red.jackf.chesttracker.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.util.Identifier;

import static red.jackf.chesttracker.ChestTracker.id;

public class OpenItemListButton extends TexturedButtonWidget {
    private static final Identifier TEXTURE = id("gui_button_small.png");

    public OpenItemListButton(int x, int y) {
        super(x, y, 9, 9, 0, 0, 9, TEXTURE, 9, 18, (button) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.openScreen(new ItemListScreen());
        });
    }
}
