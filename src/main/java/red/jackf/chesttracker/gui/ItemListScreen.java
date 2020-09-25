package red.jackf.chesttracker.gui;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.gui.widgets.WBevelledButton;
import red.jackf.chesttracker.gui.widgets.WItemListPanel;
import red.jackf.chesttracker.gui.widgets.WUpdatableTextField;
import red.jackf.chesttracker.memory.MemoryDatabase;
import red.jackf.chesttracker.memory.MemoryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        private static final Map<Identifier, ItemStack> knownIcons = new HashMap<>();
        private static final int SIDE_PADDING = 0;
        private static final int TOP_PADDING = 36;
        private static final int LEFT_ADDITIONAL_PADDING = 24;

        static {
            knownIcons.put(DimensionType.OVERWORLD_ID, new ItemStack(Items.GRASS_BLOCK));
            knownIcons.put(DimensionType.THE_NETHER_ID, new ItemStack(Items.NETHERRACK));
            knownIcons.put(DimensionType.THE_END_ID, new ItemStack(Items.END_STONE));
            knownIcons.put(MemoryUtils.ENDER_CHEST_ID, new ItemStack(Items.ENDER_CHEST));
        }

        private final WLabel counter;
        private final WItemListPanel itemPanel;

        private final Identifier currentWorldId;

        private final Map<Identifier, WBevelledButton> dimensionFilters = new HashMap<>();
        private Identifier selectedDimensionFilter = DimensionType.OVERWORLD_ID;

        public Gui() {
            @SuppressWarnings({"ConstantExpression", "PointlessArithmeticExpression"})
            int width = (18 * ChestTracker.CONFIG.visualOptions.columnCount) + (2 * SIDE_PADDING) + LEFT_ADDITIONAL_PADDING;
            int height = (18 * ChestTracker.CONFIG.visualOptions.rowCount) + SIDE_PADDING + TOP_PADDING;

            MinecraftClient mc = MinecraftClient.getInstance();

            if (mc.world != null) {
                this.currentWorldId = mc.world.getRegistryKey().getValue();
            } else {
                this.currentWorldId = DimensionType.OVERWORLD_ID;
            }

            WPlainPanel root = new WPlainPanel();
            root.setSize(width, height);
            setRootPanel(root);

            // Item List
            this.itemPanel = new WItemListPanel(ChestTracker.CONFIG.visualOptions.columnCount, ChestTracker.CONFIG.visualOptions.rowCount);
            root.add(itemPanel, SIDE_PADDING + LEFT_ADDITIONAL_PADDING, TOP_PADDING, 18 * ChestTracker.CONFIG.visualOptions.columnCount, 18 * ChestTracker.CONFIG.visualOptions.rowCount);

            /*List<ItemRepresentation> stacks = new ArrayList<>();
            for (int i = 0; i < Registry.ITEM.stream().count() - 1; i++) {
                ItemRepresentation representation = new ItemRepresentation(new ItemStack(Registry.ITEM.get(i + 1)), id("default"));
                stacks.add(representation);
            }*/

            // Title
            root.add(new WLabel(new TranslatableText("chesttracker.gui.title")), LEFT_ADDITIONAL_PADDING, 0);

            // Search Field
            WUpdatableTextField searchField = new WUpdatableTextField(new TranslatableText("chesttracker.gui.search_field_start"));
            searchField.setOnTextChanged(itemPanel::setFilter);
            root.add(searchField, SIDE_PADDING + LEFT_ADDITIONAL_PADDING, TOP_PADDING - 24, 18 * (ChestTracker.CONFIG.visualOptions.columnCount - 2) - 1, 20);

            // Page Buttons
            WBevelledButton previousButton = new WBevelledButton(new TextureIcon(LEFT_BUTTON), new TranslatableText("chesttracker.gui.previous_page"), false);
            WBevelledButton nextButton = new WBevelledButton(new TextureIcon(RIGHT_BUTTON), new TranslatableText("chesttracker.gui.next_page"), false);
            previousButton.setOnClick(itemPanel::previousPage);
            nextButton.setOnClick(itemPanel::nextPage);
            root.add(previousButton, width - SIDE_PADDING - 35, TOP_PADDING - 22, 16, 16);
            root.add(nextButton, width - SIDE_PADDING - 17, TOP_PADDING - 22, 16, 16);

            // Page Count
            counter = new WLabel(new LiteralText("not loaded")) {
                @Override
                public void addTooltip(TooltipBuilder tooltip) {
                    super.addTooltip(tooltip);
                    tooltip.add(new TranslatableText("chesttracker.gui.scroll_tip"));
                }
            };
            root.add(counter, width - SIDE_PADDING - 80, 0, 80, 12);
            counter.setHorizontalAlignment(HorizontalAlignment.RIGHT);

            // Dimension Filters
            MemoryDatabase database = MemoryDatabase.getCurrent();
            if (database != null) {
                database.getDimensions().forEach(id -> dimensionFilters.put(id, new WBevelledButton(new ItemIcon(knownIcons.getOrDefault(id, new ItemStack(Items.CRAFTING_TABLE))), new LiteralText(id.toString()), false)));
            }

            if (!dimensionFilters.containsKey(this.currentWorldId)) {
                dimensionFilters.put(this.currentWorldId, new WBevelledButton(new ItemIcon(knownIcons.getOrDefault(this.currentWorldId, new ItemStack(Items.CRAFTING_TABLE))), new LiteralText(this.currentWorldId.toString()), false));
            }

            dimensionFilters.get(this.currentWorldId).setHighlighted(true);
            //dimensionFilters.put(DimensionType.THE_NETHER_ID, new WBevelledButton(new ItemIcon(Items.NETHERRACK.getStackForRender()), new LiteralText(DimensionType.THE_NETHER_ID.toString())));
            //dimensionFilters.put(DimensionType.THE_END_ID, new WBevelledButton(new ItemIcon(Items.END_STONE.getStackForRender()), new LiteralText(DimensionType.THE_END_ID.toString())));

            int i = 0;
            for (Map.Entry<Identifier, WBevelledButton> entry : dimensionFilters.entrySet()) {
                entry.getValue().setOnClick(() -> {
                    setDimensionFilter(entry.getKey());
                    mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                });
                root.add(entry.getValue(), SIDE_PADDING, 20 * i, 18, 18);
                i++;
            }

            // Wrap up

            itemPanel.setPageChangeHook((current, max) -> {
                counter.setText(new TranslatableText("chesttracker.gui.page_count", current, max));
                previousButton.setEnabled(current != 1);
                nextButton.setEnabled(!current.equals(max));
            });

            root.validate(this);
            setDimensionFilter(currentWorldId);
        }

        private void setItems(List<ItemStack> items) {
            this.itemPanel.setItems(items);
        }

        private void setDimensionFilter(Identifier newId) {
            WBevelledButton current = dimensionFilters.get(selectedDimensionFilter);
            if (current != null) current.setPressed(false);
            if (dimensionFilters.containsKey(newId)) {
                dimensionFilters.get(newId).setPressed(true);
            } else {
                dimensionFilters.get(DimensionType.OVERWORLD_ID).setPressed(true);
            }
            selectedDimensionFilter = newId;

            MemoryDatabase database = MemoryDatabase.getCurrent();
            if (database != null) {
                setItems(database.getItems(selectedDimensionFilter));
                this.itemPanel.setUsable(selectedDimensionFilter.equals(currentWorldId));
            } else {
                counter.setText(new TranslatableText("chesttracker.gui.error_loading"));
            }
        }
    }
}
