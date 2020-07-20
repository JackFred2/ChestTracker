package red.jackf.chesttracker.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.tracker.LocationStorage;
import spinnery.client.screen.BaseScreen;
import spinnery.widget.WInterface;
import spinnery.widget.WPanel;
import spinnery.widget.WTextField;
import spinnery.widget.WVerticalScrollableContainer;
import spinnery.widget.api.Position;
import spinnery.widget.api.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemManagerScreen extends BaseScreen {
    private final int width = 178;
    private final int height = 207;

    private final WPanel mainPanel;
    private final WVerticalScrollableContainer scrollArea;
    private final List<ItemStack> list;
    private final WTextField searchField;

    public ItemManagerScreen() {
        WInterface mainInterface = getInterface();

        mainPanel = mainInterface.createChild(WPanel::new, Position.ORIGIN, Size.of(width, height))
            .setLabel(new TranslatableText("chesttracker.gui.title"));
        mainPanel.center();
        mainPanel.setPosition(mainPanel.getPosition().add(-6, 0, 0));

        scrollArea = mainPanel.createChild(WVerticalScrollableContainer::new, Position.of(mainPanel).add(3, 22, 0), Size.of(width + 4, height - 26))
            .setDivisionSpace(1)
            .setHasFade(false)
            .setScrollbarWidth(8);

        searchField = mainPanel.createChild(WClearedTextField::new, Position.of(mainPanel.getX() + width - 98, mainPanel.getY() + 4, 0), Size.of(94, 18));
        searchField.setFixedLength(15);
        searchField.setText("Filter..");

        list = LocationStorage.get().getItems(MinecraftClient.getInstance().world.getRegistryKey().getValue());

        searchField.setOnCharTyped((widget, charTyped, keycode) -> update());

        searchField.setOnKeyPressed(((widget, keyPressed, character, keyModifier) -> {
            switch (keyPressed) {
                case GLFW.GLFW_KEY_DELETE:
                case GLFW.GLFW_KEY_BACKSPACE:
                    update();
                    break;
            }
        }));

        setChildren(list);
    }

    private void update() {
        List<ItemStack> stacks = list.stream().filter(stack ->
            stack.getName().getString().toLowerCase().contains(searchField.getText().toLowerCase())).collect(Collectors.toList());
        setChildren(stacks);
    }

    private void setChildren(List<ItemStack> slots) {
        scrollArea.getWidgets().clear();
        if (slots.size() == 0) return;
        List<WGhostSlot> tempSlots = new ArrayList<>(9);
        for (int i = 0; i < slots.size(); i++) {
            WGhostSlot slot = new WGhostSlot(
                slots.get(i),
                Position.of(Position.ORIGIN)
            );
            //slot.updatePos(mainPanel.getX(), mainPanel.getY(), mainPanel.getZ());
            tempSlots.add(slot);
            if (tempSlots.size() == 9 || i == slots.size() - 1) {
                scrollArea.addRow(tempSlots.toArray(new WGhostSlot[0]));
                tempSlots.clear();
            }
        }
    }

    public int getX() {
        return (int) mainPanel.getX();
    }

    public int getY() {
        return (int) mainPanel.getY();
    }

    public int getWidth() {
        return width + 7;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        mainPanel.center();
        mainPanel.setPosition(mainPanel.getPosition().add(-6, 0, 0));
        mainPanel.getWidgets().forEach(w -> {
            if (w instanceof WGhostSlot)
                ((WGhostSlot) w).updatePos(mainPanel.getX(), mainPanel.getY(), mainPanel.getZ());
        });
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float tick) {
        super.render(matrices, mouseX, mouseY, tick);
        scrollArea.getWidgets().forEach(w -> {
            if (w instanceof WGhostSlot) {
                WGhostSlot slot = (WGhostSlot) w;
                if (w.isWithinBounds(mouseX, mouseY) && !w.isHidden()) {
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
        } else if (!searchField.isActive() && MinecraftClient.getInstance().options.keyInventory.matchesKey(keyCode, character)) {
            if (MinecraftClient.getInstance().player != null)
                MinecraftClient.getInstance().player.closeHandledScreen();
            return true;
        }
        return true;
    }
}
