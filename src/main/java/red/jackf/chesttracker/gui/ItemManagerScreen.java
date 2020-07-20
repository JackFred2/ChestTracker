package red.jackf.chesttracker.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import red.jackf.chesttracker.tracker.LocationStorage;
import spinnery.client.screen.BaseScreen;
import spinnery.widget.WInterface;
import spinnery.widget.WPanel;
import spinnery.widget.api.Position;
import spinnery.widget.api.Size;

import java.util.List;

public class ItemManagerScreen extends BaseScreen {
    private final int width = 176;
    private final int height = 166;

    private final WPanel mainPanel;
    private final List<ItemStack> list;

    public ItemManagerScreen() {
        WInterface mainInterface = getInterface();

        mainPanel = mainInterface.createChild(WPanel::new, Position.ORIGIN, Size.of(width, height))
            .setLabel(new TranslatableText("chesttracker.gui.title"));
        mainPanel.center();

        list = LocationStorage.get().getItems(MinecraftClient.getInstance().world.getRegistryKey().getValue());

        for (int i = 0; i < list.size(); i++) {
            WGhostSlot slot = new WGhostSlot(
                list.get(i),
                Position.of(8 + 18 * (i % 9), 18 + 18 * Math.floorDiv(i, 9), 100f)
            );
            slot.updatePos(mainPanel.getX(), mainPanel.getY(), mainPanel.getZ());
            mainPanel.add(slot);
        }


    }

    public int getX() {
        return (int) mainPanel.getX();
    }

    public int getY() {
        return (int) mainPanel.getY();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        mainPanel.center();
        mainPanel.getWidgets().forEach(w -> {
            if (w instanceof WGhostSlot)
                ((WGhostSlot) w).updatePos(mainPanel.getX(), mainPanel.getY(), mainPanel.getZ());
        });
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float tick) {
        super.render(matrices, mouseX, mouseY, tick);
        mainPanel.getWidgets().forEach(w -> {
            if (w instanceof WGhostSlot) {
                WGhostSlot slot = (WGhostSlot) w;
                if (w.isWithinBounds(mouseX, mouseY)) {
                    slot.hover = true;
                    this.renderTooltip(matrices, slot.item, mouseX, mouseY);
                } else {
                    slot.hover = false;
                }
            }
        });
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
