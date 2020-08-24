package red.jackf.chesttracker.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import static red.jackf.chesttracker.ChestTracker.id;

@Environment(EnvType.CLIENT)
public class OpenItemListButton extends TexturedButtonWidget {
    private static final Identifier TEXTURE = id("textures/gui_button_small.png");
    private final HandledScreen<?> screen;

    public OpenItemListButton(HandledScreen<?> screen) {
        super(0, 0, 9, 9, 0, 0, 9, TEXTURE, 9, 18, (button) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen != null) client.currentScreen.onClose();
            client.openScreen(new ItemListScreen());
        });
        this.screen = screen;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.resize();
        super.render(matrices, mouseX, mouseY, delta);
        if (this.isMouseOver(mouseX, mouseY)) {
            this.screen.renderTooltip(matrices, new TranslatableText("chesttracker.gui.title"), mouseX, mouseY);
        }
    }

    private void resize() {
        this.setPos(ButtonPositions.getX(screen, 0), ButtonPositions.getY(screen, 0));
    }
}
