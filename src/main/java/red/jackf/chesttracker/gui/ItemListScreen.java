package red.jackf.chesttracker.gui;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.widgets.WHeldButton;
import red.jackf.chesttracker.gui.widgets.WItemListPanel;
import red.jackf.chesttracker.gui.widgets.WPageButton;
import red.jackf.chesttracker.gui.widgets.WUpdatableTextField;
import red.jackf.chesttracker.memory.MemoryDatabase;
import red.jackf.chesttracker.memory.MemoryUtils;
import red.jackf.chesttracker.mixins.AccessorWTabPanel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static red.jackf.chesttracker.ChestTracker.id;

@Environment(EnvType.CLIENT)
public class ItemListScreen extends CottonClientScreen {
    public ItemListScreen() {
        super(new Gui());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (MinecraftClient.getInstance().options.inventoryKey.matchesKey(keyCode, scanCode) && !(this.description.getFocus() instanceof WTextField)) {
            this.close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public static class Gui extends LightweightGuiDescription {
        private static final Map<Identifier, ItemStack> knownIcons = new HashMap<>();
        private static final int SIDE_PADDING = 0;
        private static final int TOP_PADDING = 36;
        private static final int LEFT_ADDITIONAL_PADDING = 0;
        private static final int BEVEL_PADDING = 6;
        private static final int BOTTOM_PADDING = 17;

        private static final LiteralText BLANK_TEXT = new LiteralText("-");

        static {
            knownIcons.put(DimensionType.OVERWORLD_ID, new ItemStack(Items.GRASS_BLOCK));
            knownIcons.put(DimensionType.THE_NETHER_ID, new ItemStack(Items.NETHERRACK));
            knownIcons.put(DimensionType.THE_END_ID, new ItemStack(Items.END_STONE));
            knownIcons.put(MemoryUtils.ENDER_CHEST_ID, new ItemStack(Items.ENDER_CHEST));
        }

        private final Map<Identifier, WItemListPanel> ITEM_LISTS = new HashMap<>();

        public Gui() {
            @SuppressWarnings({"ConstantExpression", "PointlessArithmeticExpression"})
            int width = (18 * ChestTracker.CONFIG.visualOptions.columnCount) + (2 * SIDE_PADDING) + LEFT_ADDITIONAL_PADDING + (2 * BEVEL_PADDING);
            int height = (18 * ChestTracker.CONFIG.visualOptions.rowCount) + SIDE_PADDING + TOP_PADDING + (2 * BEVEL_PADDING) + (ChestTracker.CONFIG.visualOptions.hideDeleteButton ? 0 : BOTTOM_PADDING);

            MinecraftClient mc = MinecraftClient.getInstance();

            // Search
            WTabPanel tabPanel = new WTabPanel();
            tabPanel.setSize(width, height);
            setRootPanel(tabPanel);

            // Dimension Filters
            MemoryDatabase database = MemoryDatabase.getCurrent();
            if (database != null) {

                Identifier currentWorld;
                int selectedTabIndex = -1;
                int currentIndex = 0;

                if (mc.world != null) {
                    currentWorld = mc.world.getRegistryKey().getValue();
                } else {
                    currentWorld = DimensionType.OVERWORLD_ID;
                }

                for (Identifier id : database.getDimensions()) {
                    var dimensionPanel = new WPlainPanel() {
                        private WUpdatableTextField searchBar = null;

                        @Override
                        public void onShown() {
                            super.onShown();
                            if (this.searchBar != null) this.searchBar.requestFocus();
                        }
                    };
                    dimensionPanel.setSize(width, height);
                    tabPanel.add(new WTabPanel.Tab.Builder(dimensionPanel)
                        .icon(new ItemIcon(knownIcons.getOrDefault(id, new ItemStack(Items.CRAFTING_TABLE))))
                        .tooltip(new LiteralText(id.toString()))
                        .build());

                    // Item List
                    WItemListPanel itemList = new WItemListPanel(ChestTracker.CONFIG.visualOptions.columnCount, ChestTracker.CONFIG.visualOptions.rowCount);
                    dimensionPanel.add(itemList, SIDE_PADDING + LEFT_ADDITIONAL_PADDING + BEVEL_PADDING, TOP_PADDING + BEVEL_PADDING, 18 * ChestTracker.CONFIG.visualOptions.columnCount, 18 * ChestTracker.CONFIG.visualOptions.rowCount);
                    ITEM_LISTS.put(id, itemList);

                    // Title
                    dimensionPanel.add(new WLabel(new TranslatableText("chesttracker.gui.title")), LEFT_ADDITIONAL_PADDING + BEVEL_PADDING, BEVEL_PADDING);

                    // Search Field
                    WUpdatableTextField searchField = new WUpdatableTextField(new TranslatableText("chesttracker.gui.search_field_start"));
                    searchField.setOnTextChanged(itemList::setFilter);
                    dimensionPanel.searchBar = searchField;
                    searchField.setHost(this);
                    dimensionPanel.add(searchField, SIDE_PADDING + LEFT_ADDITIONAL_PADDING + BEVEL_PADDING, TOP_PADDING - 24 + BEVEL_PADDING, 18 * (ChestTracker.CONFIG.visualOptions.columnCount - 2) - 1, 20);

                    // Page Buttons
                    WPageButton previousButton = new WPageButton(true, new TranslatableText("chesttracker.gui.previous_page"), false);
                    WPageButton nextButton = new WPageButton(false, new TranslatableText("chesttracker.gui.next_page"), false);
                    previousButton.setOnClick(itemList::previousPage);
                    nextButton.setOnClick(itemList::nextPage);
                    dimensionPanel.add(previousButton, width - SIDE_PADDING - 35 - BEVEL_PADDING, TOP_PADDING - 22 + BEVEL_PADDING, 16, 16);
                    dimensionPanel.add(nextButton, width - SIDE_PADDING - 17 - BEVEL_PADDING, TOP_PADDING - 22 + BEVEL_PADDING, 16, 16);

                    // Page Count
                    WLabel counter = new WLabel(new LiteralText("not loaded")) {
                        @Override
                        public void addTooltip(TooltipBuilder tooltip) {
                            super.addTooltip(tooltip);
                            tooltip.add(new TranslatableText("chesttracker.gui.scroll_tip"));
                        }
                    };
                    dimensionPanel.add(counter, width - SIDE_PADDING - 80 - BEVEL_PADDING, BEVEL_PADDING, 80, 12);
                    counter.setHorizontalAlignment(HorizontalAlignment.RIGHT);

                    // Wrap up
                    itemList.setPageChangeHook((current, max) -> {
                        counter.setText(new TranslatableText("chesttracker.gui.page_count", current, max));
                        previousButton.setEnabled(current != 1);
                        nextButton.setEnabled(!current.equals(max));
                    });

                    itemList.setItems(database.getItems(id));

                    if (!ChestTracker.CONFIG.visualOptions.hideDeleteButton) {
                        // Reset Button
                        WHeldButton resetButton = new WHeldButton(new TranslatableText("chesttracker.gui.reset_button"), new TranslatableText("chesttracker.gui.reset_button_alt"), 40);
                        dimensionPanel.add(resetButton, BEVEL_PADDING, height - 20, width - BEVEL_PADDING * 2, 20);
                        resetButton.setOnClick(() -> {
                            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            database.clearDimension(id);
                            itemList.setItems(Collections.emptyList());
                        });
                    }

                    if (id.equals(currentWorld)) {
                        selectedTabIndex = currentIndex;
                    }
                    currentIndex++;
                }

                // Settings Panel
                WPlainPanel settingsPanel = new WPlainPanel();
                settingsPanel.setSize(width, height);
                var tab = new WTabPanel.Tab.Builder(settingsPanel)
                    .tooltip(new TranslatableText("chesttracker.gui.settings"))
                    .icon(new TextureIcon(id("textures/icon.png")))
                    .build();

                tabPanel.add(tab);

                // Toggle Remember Button
                WLabel addNewChestsToggleLabel = new WLabel(new TranslatableText("chesttracker.gui.settings.remember_chests"));
                settingsPanel.add(addNewChestsToggleLabel, BEVEL_PADDING, BEVEL_PADDING);
                WToggleButton addNewChestsToggle = new WToggleButton();
                settingsPanel.add(addNewChestsToggle, width - BEVEL_PADDING - 17, BEVEL_PADDING - 5);
                addNewChestsToggle.setToggle(ChestTracker.CONFIG.miscOptions.rememberNewChests);
                addNewChestsToggle.setOnToggle(newValue -> {
                    ChestTracker.CONFIG.miscOptions.rememberNewChests = newValue;
                    AutoConfig.getConfigHolder(ChestTrackerConfig.class).save();
                });

                // Delete Unnamed button
                WHeldButton deleteUnnamed = new WHeldButton(new TranslatableText("chesttracker.gui.delete_unnamed"), new TranslatableText("chesttracker.gui.reset_button_alt"), 30);
                settingsPanel.add(deleteUnnamed, BEVEL_PADDING, BEVEL_PADDING + 12, width - (BEVEL_PADDING * 2), 20);

                deleteUnnamed.setOnClick(() -> {
                    if (mc.player == null) return;
                    database.getAllMemories(currentWorld).stream()
                        .filter(memory -> memory.getTitle() == null && memory.getPosition() != null)
                        .forEach(memory -> database.removePos(currentWorld, memory.getPosition()));
                    updateItemList(database, currentWorld);
                });

                // Range Slider
                WLabeledSlider rangeSlider = new WLabeledSlider(1, 98);
                settingsPanel.add(rangeSlider, BEVEL_PADDING, BEVEL_PADDING + 36);
                rangeSlider.setSize(width - BEVEL_PADDING * 2, 20);
                rangeSlider.setLabelUpdater(Gui::getSliderText);
                rangeSlider.setValue(ChestTracker.CONFIG.miscOptions.searchRange, true);
                rangeSlider.setDraggingFinishedListener(value -> {
                    ChestTracker.CONFIG.miscOptions.searchRange = value;
                    AutoConfig.getConfigHolder(ChestTrackerConfig.class).save();
                });
                rangeSlider.setLabel(getSliderText(ChestTracker.CONFIG.miscOptions.searchRange));

                // Delete Outside & Inside Range buttons
                WHeldButton deleteOutside = new WHeldButton(BLANK_TEXT, new TranslatableText("chesttracker.gui.reset_button_alt"), 30);
                settingsPanel.add(deleteOutside, BEVEL_PADDING, BEVEL_PADDING + 60, width - (BEVEL_PADDING * 2), 20);

                WHeldButton deleteInside = new WHeldButton(BLANK_TEXT, new TranslatableText("chesttracker.gui.reset_button_alt"), 30);
                settingsPanel.add(deleteInside, BEVEL_PADDING, BEVEL_PADDING + 84, width - (BEVEL_PADDING * 2), 20);

                rangeSlider.setValueChangeListener(value -> updateDeleteButtonLabels(deleteInside, deleteOutside, value));
                updateDeleteButtonLabels(deleteInside, deleteOutside, ChestTracker.CONFIG.miscOptions.searchRange);

                deleteInside.setOnClick(() -> {
                    if (mc.player == null) return;
                    database.getAllMemories(currentWorld).stream()
                        .filter(memory -> memory.getPosition() != null && memory.getPosition().getSquaredDistance(mc.player.getBlockPos()) <= ChestTracker.getSquareSearchRange())
                        .forEach(memory -> database.removePos(currentWorld, memory.getPosition()));
                    updateItemList(database, currentWorld);
                });

                deleteOutside.setOnClick(() -> {
                    if (mc.player == null) return;
                    database.getAllMemories(currentWorld).stream()
                        .filter(memory -> memory.getPosition() != null && memory.getPosition().getSquaredDistance(mc.player.getBlockPos()) > ChestTracker.getSquareSearchRange())
                        .forEach(memory -> database.removePos(currentWorld, memory.getPosition()));
                    updateItemList(database, currentWorld);
                });

                var showAll = new WButton(new TranslatableText("chesttracker.gui.show_all"));
                settingsPanel.add(showAll, BEVEL_PADDING, BEVEL_PADDING + 108, width - (BEVEL_PADDING * 2), 20);

                showAll.setOnClick(() -> ChestTracker.startRenderingForLocations(database.getAllMemories(currentWorld)));

                // Dimension Label
                WLabel dimensionLabel = new WLabel(new LiteralText(currentWorld.toString()));
                settingsPanel.add(dimensionLabel, BEVEL_PADDING, height - BEVEL_PADDING, 80, 12);

                if (!ChestTracker.CONFIG.visualOptions.hideDatabaseInfo) {
                    WLabel databaseName = new WLabel(new LiteralText(database.getId())) {
                        @Override
                        public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
                            this.setSize( mc.textRenderer.getWidth(this.text) + 12, 18);
                            BackgroundPainter.VANILLA.paintBackground(matrices, x - 4, y + 19, this);
                            super.paint(matrices, x + 2, y + 24, mouseX, mouseY);
                        }
                    };
                    settingsPanel.add(databaseName, BEVEL_PADDING - 2, height - BEVEL_PADDING - 6);
                }

                // Set tab
                if (selectedTabIndex == -1) {
                    // go to settings panel if our current dimension has no entries
                    //noinspection ConstantConditions
                    ((AccessorWTabPanel) tabPanel).getMainPanel().setSelectedIndex(currentIndex);
                } else {
                    // go to current dimension's panel
                    //noinspection ConstantConditions
                    ((AccessorWTabPanel) tabPanel).getMainPanel().setSelectedIndex(selectedTabIndex);
                }
            }
        }

        private void updateItemList(MemoryDatabase database, Identifier currentWorld) {
            if (ITEM_LISTS.containsKey(currentWorld)) ITEM_LISTS.get(currentWorld).setItems(database.getItems(currentWorld));
        }

        private static void updateDeleteButtonLabels(WHeldButton deleteInside, WHeldButton deleteOutside, int rawValue) {
            if (rawValue == 98) {
                deleteInside.setEnabled(false);
                deleteOutside.setEnabled(false);
                deleteInside.setText(BLANK_TEXT);
                deleteOutside.setText(BLANK_TEXT);
            } else {
                deleteInside.setEnabled(true);
                deleteOutside.setEnabled(true);
                deleteInside.setText(new TranslatableText("chesttracker.gui.delete_inside_range", ChestTracker.sliderValueToRange(rawValue)));
                deleteOutside.setText(new TranslatableText("chesttracker.gui.delete_outside_range", ChestTracker.sliderValueToRange(rawValue)));
            }
        }

        private static Text getSliderText(int sliderValue) {
            int finalValue = ChestTracker.sliderValueToRange(sliderValue);
            return new TranslatableText("chesttracker.gui.range").append(finalValue == Integer.MAX_VALUE
                ? new TranslatableText("options.framerateLimit.max")
                : new TranslatableText("chesttracker.gui.block_range", finalValue)
            );
        }

    }
}
