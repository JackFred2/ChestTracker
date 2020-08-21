package red.jackf.chesttracker.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static red.jackf.chesttracker.ChestTracker.id;

@Environment(EnvType.CLIENT)
public class ItemListScreen extends CottonClientScreen {
    public ItemListScreen() {
        super(new Gui());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (MinecraftClient.getInstance().options.keyInventory.matchesKey(keyCode, scanCode)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public static class Gui extends LightweightGuiDescription {
        private static final int COLUMNS = 9;
        private static final int DEFAULT_ROWS = 9;
        private static final int SIDE_PADDING = 2;
        private static final int TOP_PADDING = 30;

        private final WItemGridPanel scrolledPanel;

        public Gui() {
            WPlainPanel root = new WPlainPanel();
            setRootPanel(root);

            root.setSize((18 * COLUMNS) + (2 * SIDE_PADDING), (18 * DEFAULT_ROWS) + SIDE_PADDING + TOP_PADDING);

            scrolledPanel = new WItemGridPanel();
            scrolledPanel.setSize(18 * COLUMNS, 18 * DEFAULT_ROWS * 2);
            WScrollPanel scroller = new WScrollPanel(scrolledPanel);
            scroller.setScrollingHorizontally(TriState.FALSE);
            scroller.setScrollingVertically(TriState.TRUE);
            root.add(scroller, SIDE_PADDING, TOP_PADDING, 18 * COLUMNS + 12, 18 * DEFAULT_ROWS);

            List<ItemRepresentation> stacks = new ArrayList<>();
            for (int i = 0; i < Registry.ITEM.stream().count() - 1; i++) {
                ItemRepresentation representation = new ItemRepresentation(new ItemStack(Registry.ITEM.get(i + 1)), id("default"));
                if (new Random().nextFloat() < 0.1f) representation.setVisible(false);
                stacks.add(representation);
            }

            setItems(stacks);

            root.validate(this);
        }

        private void setItems(List<ItemRepresentation> items) {
            this.scrolledPanel.setItems(items);
            this.scrolledPanel.setSize(this.scrolledPanel.getWidth(), 18 * Math.max(DEFAULT_ROWS, 1 + (items.size() - 1) / 9));
        }
    }

    public static class WItemGridPanel extends WGridPanel {
        private static final Identifier SLOT = id("slot.png");
        private static final Identifier SLOT_RED = id("slot_red.png");
        private List<ItemRepresentation> items = Collections.emptyList();

        public void clear() {
            this.items.clear();
        }

        public void setItems(@NotNull List<ItemRepresentation> items) {
            this.items = items;
        }

        @Override
        public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
            super.paint(matrices, x, y, mouseX, mouseY);

            RenderSystem.enableDepthTest();
            MinecraftClient mc = MinecraftClient.getInstance();
            ItemRenderer renderer = mc.getItemRenderer();

            for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
                ItemRepresentation representation = items.get(i);
                int renderX = x + 18 * (i % Gui.COLUMNS);
                int renderY = y + (18 * (i / Gui.COLUMNS));

                mc.getTextureManager().bindTexture(representation.isVisible ? SLOT : SLOT_RED);
                DrawableHelper.drawTexture(matrices, renderX, renderY, 10, 0, 0, 18, 18, 18, 18);

                renderer.zOffset = 100f;
                renderer.renderInGui(representation.stack, renderX + 1, renderY + 1);
                renderer.zOffset = 0f;
            }
        }

        @Override
        public void renderTooltip(MatrixStack matrices, int x, int y, int tX, int tY) {
            super.renderTooltip(matrices, x, y, tX, tY);
        }
    }

    public static class ItemRepresentation {
        private final ItemStack stack;
        private final Identifier dimensionId;
        private boolean isVisible = true;

        private ItemRepresentation(@NotNull ItemStack stack, @NotNull Identifier dimensionId) {
            this.stack = stack;
            this.dimensionId = dimensionId;
        }

        private void setVisible(boolean visible) {
            isVisible = visible;
        }

        private boolean isVisible() {
            return isVisible;
        }

        private Identifier getDimensionId() {
            return dimensionId;
        }

        private ItemStack getStack() {
            return stack;
        }
    }
}
