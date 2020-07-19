package red.jackf.chesttracker.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import spinnery.client.screen.BaseScreen;
import spinnery.widget.WInterface;
import spinnery.widget.WPanel;
import spinnery.widget.api.Position;
import spinnery.widget.api.Size;

public class ItemManagerScreen extends BaseScreen {
    private int width = 176;
    private int height = 166;

    private WPanel mainPanel;

    public ItemManagerScreen() {
        WInterface mainInterface = getInterface();

        mainPanel = mainInterface.createChild(WPanel::new, Position.ORIGIN, Size.of(width, height))
            .setLabel(new TranslatableText("chesttracker.gui.title"));
        mainPanel.center();
        mainPanel.setZ(0);

    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float tick) {
        super.render(matrices, mouseX, mouseY, tick);

        for (int x = 0; x < 9; x++) {

            this.itemRenderer.renderGuiItemOverlay(this.textRenderer,
                new ItemStack(Items.DIAMOND, 4),
                (int) (mainPanel.getX() + 8 + 18 * x),
                (int) (mainPanel.getY() + 8));
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        mainPanel.center();
    }

    @Override
    public boolean keyPressed(int keyCode, int character, int keyModifier) {
        if (super.keyPressed(keyCode, character, keyModifier)) {
            return true;
        } else if (MinecraftClient.getInstance().options.keyInventory.matchesKey(keyCode, character)) {
            if (MinecraftClient.getInstance().player != null)
                MinecraftClient.getInstance().player.closeHandledScreen();
            return true;
        }
        return true;
    }
}
