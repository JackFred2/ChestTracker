package red.jackf.chesttracker.gui;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.ChestTracker;
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

        private static final int SIDE_PADDING = 0;
        private static final int TOP_PADDING = 36;

        private final WItemListPanel itemPanel;

        public Gui() {
            @SuppressWarnings({"ConstantExpression", "PointlessArithmeticExpression"})
            int width = (18 * ChestTracker.CONFIG.visualOptions.columnCount) + (2 * SIDE_PADDING);
            int height = (18 * ChestTracker.CONFIG.visualOptions.rowCount) + SIDE_PADDING + TOP_PADDING;

            WPlainPanel root = new WPlainPanel();
            root.setSize(width, height);
            setRootPanel(root);

            // Item List
            itemPanel = new WItemListPanel(ChestTracker.CONFIG.visualOptions.columnCount, ChestTracker.CONFIG.visualOptions.rowCount);
            root.add(itemPanel, SIDE_PADDING, TOP_PADDING, 18 * ChestTracker.CONFIG.visualOptions.columnCount, 18 * ChestTracker.CONFIG.visualOptions.rowCount);

            List<ItemRepresentation> stacks = new ArrayList<>();
            for (int i = 0; i < Registry.ITEM.stream().count() - 1; i++) {
                ItemRepresentation representation = new ItemRepresentation(new ItemStack(Registry.ITEM.get(i + 1)), id("default"));
                stacks.add(representation);
            }

            // Title
            root.add(new WLabel(new TranslatableText("chesttracker.gui.title")), 0, 0);

            // Search Field
            WUpdatableTextField searchField = new WUpdatableTextField(new TranslatableText("chesttracker.gui.search_field_start"));
            searchField.setOnTextChanged(itemPanel::setFilter);
            root.add(searchField, SIDE_PADDING, TOP_PADDING - 24, 18 * (ChestTracker.CONFIG.visualOptions.columnCount - 2) - 1, 20);

            // Page Buttons
            WBevelledButton previousButton = new WBevelledButton(new TextureIcon(LEFT_BUTTON), new TranslatableText("chesttracker.gui.previous_page"));
            WBevelledButton nextButton = new WBevelledButton(new TextureIcon(RIGHT_BUTTON), new TranslatableText("chesttracker.gui.next_page"));
            previousButton.setIconSize(16);
            nextButton.setIconSize(16);
            previousButton.setOnClick(itemPanel::previousPage);
            nextButton.setOnClick(itemPanel::nextPage);
            root.add(previousButton, width - SIDE_PADDING - 35, TOP_PADDING - 22, 16, 16);
            root.add(nextButton, width - SIDE_PADDING - 17, TOP_PADDING - 22, 16, 16);

            // Page Count
            WLabel count = new WLabel(new LiteralText("not loaded")) {
                @Override
                public void addTooltip(TooltipBuilder tooltip) {
                    super.addTooltip(tooltip);
                    tooltip.add(new TranslatableText("chesttracker.gui.scroll_tip"));
                }
            };
            root.add(count, width - SIDE_PADDING - 80, 0, 80, 12);
            count.setHorizontalAlignment(HorizontalAlignment.RIGHT);

            itemPanel.setPageChangeHook((current, max) -> {
                count.setText(new TranslatableText("chesttracker.gui.page_count", current, max));
                previousButton.setEnabled(current != 1);
                nextButton.setEnabled(current != max);
            });

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
