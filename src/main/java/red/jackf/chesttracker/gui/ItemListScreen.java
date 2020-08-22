package red.jackf.chesttracker.gui;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.gui.widgets.WBevelledButton;
import red.jackf.chesttracker.gui.widgets.WItemListPanel;
import red.jackf.chesttracker.gui.widgets.WUpdatableTextField;

import java.util.ArrayList;
import java.util.List;

import static red.jackf.chesttracker.ChestTracker.id;

@Environment(EnvType.CLIENT)
public class ItemListScreen extends CottonClientScreen {
    public ItemListScreen() {
        super(new Gui());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (MinecraftClient.getInstance().options.keyInventory.matchesKey(keyCode, scanCode) && !(this.description.getFocus() instanceof WTextField)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public static class Gui extends LightweightGuiDescription {
        private static final Identifier LEFT_BUTTON = id("textures/left_button.png");
        private static final Identifier RIGHT_BUTTON = id("textures/right_button.png");

        private static final int COLUMNS = 9;
        private static final int ROWS = 6;
        private static final int SIDE_PADDING = 0;
        private static final int TOP_PADDING = 36;

        private final WItemListPanel itemPanel;

        public Gui() {
            @SuppressWarnings({"ConstantExpression", "PointlessArithmeticExpression"})
            int width = (18 * COLUMNS) + (2 * SIDE_PADDING);
            int height = (18 * ROWS) + SIDE_PADDING + TOP_PADDING;

            WPlainPanel root = new WPlainPanel();
            root.setSize(width, height);
            setRootPanel(root);

            // Item List
            itemPanel = new WItemListPanel(COLUMNS, ROWS);
            root.add(itemPanel, SIDE_PADDING, TOP_PADDING, 18 * COLUMNS, 18 * ROWS);

            List<ItemRepresentation> stacks = new ArrayList<>();
            for (int i = 0; i < Registry.ITEM.stream().count() - 1; i++) {
                ItemRepresentation representation = new ItemRepresentation(new ItemStack(Registry.ITEM.get(i + 1)), id("default"));
                stacks.add(representation);
            }

            // Title
            root.add(new WLabel(new TranslatableText("chesttracker.gui.title")), 0, 0);

            // Search Field
            WUpdatableTextField searchField = new WUpdatableTextField(new TranslatableText("chesttracker.gui.search_filed_start"));
            searchField.setOnTextChanged(itemPanel::setFilter);
            root.add(searchField, SIDE_PADDING, TOP_PADDING - 24, 18 * (COLUMNS - 2) - 1, 20);

            // Page Buttons
            WBevelledButton leftButton = new WBevelledButton(new TextureIcon(id("textures/left_button.png")), new TranslatableText("chestracker.gui.previous_page"));
            WBevelledButton rightButton = new WBevelledButton(new TextureIcon(id("textures/right_button.png")), new TranslatableText("chestracker.gui.next_page"));
            leftButton.setIconSize(16);
            rightButton.setIconSize(16);
            leftButton.setOnClick(itemPanel::previousPage);
            rightButton.setOnClick(itemPanel::nextPage);
            root.add(leftButton, width - SIDE_PADDING - 35, TOP_PADDING - 22, 16, 16);
            root.add(rightButton, width - SIDE_PADDING - 17, TOP_PADDING - 22, 16, 16);

            // Page Count
            WLabel count = new WLabel(new LiteralText("not loaded"));
            root.add(count, width - SIDE_PADDING - 100, 0, 100, 12);
            count.setHorizontalAlignment(HorizontalAlignment.RIGHT);

            itemPanel.setPageChangeHook((current, max) -> count.setText(new TranslatableText("chesttracker.gui.page_count", current, max)));

            setItems(stacks);

            root.validate(this);
        }

        private void setItems(List<ItemRepresentation> items) {
            this.itemPanel.setItems(items);
        }
    }

    /**
     * Extended item representation for the item list
     */
    public static class ItemRepresentation {
        private final ItemStack stack;
        private final Identifier memoryId;

        private ItemRepresentation(@NotNull ItemStack stack, @NotNull Identifier dimensionId) {
            this.stack = stack;
            this.memoryId = dimensionId;
        }

        private Identifier getMemoryId() {
            return memoryId;
        }

        public ItemStack getStack() {
            return stack;
        }

        @Override
        public String toString() {
            return "ItemRepresentation{" +
                "stack=" + stack +
                ", memoryId=" + memoryId +
                '}';
        }
    }
}
