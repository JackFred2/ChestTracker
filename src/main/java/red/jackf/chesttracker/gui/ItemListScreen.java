package red.jackf.chesttracker.gui;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
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
        if (MinecraftClient.getInstance().options.keyInventory.matchesKey(keyCode, scanCode) && !(this.description.getFocus() instanceof WTextField)) {
            this.onClose();
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
        private static final int BOTTOM_PADDING = 23;

        static {
            knownIcons.put(DimensionType.OVERWORLD_ID, new ItemStack(Items.GRASS_BLOCK));
            knownIcons.put(DimensionType.THE_NETHER_ID, new ItemStack(Items.NETHERRACK));
            knownIcons.put(DimensionType.THE_END_ID, new ItemStack(Items.END_STONE));
            knownIcons.put(MemoryUtils.ENDER_CHEST_ID, new ItemStack(Items.ENDER_CHEST));
        }

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
                int selectedTabIndex = 0;
                int currentIndex = 0;

                if (mc.world != null) {
                    currentWorld = mc.world.getRegistryKey().getValue();
                } else {
                    currentWorld = DimensionType.OVERWORLD_ID;
                }

                for (Identifier id : database.getDimensions()) {
                    WPlainPanel dimensionPanel = new WPlainPanel();
                    dimensionPanel.setSize(width, height);
                    tabPanel.add(new WTabPanel.Tab(null, new ItemIcon(knownIcons.getOrDefault(id, new ItemStack(Items.CRAFTING_TABLE))), dimensionPanel, (tooltip) -> tooltip.add(new LiteralText(id.toString()))));

                    if (id.equals(currentWorld)) selectedTabIndex = currentIndex;
                    currentIndex++;

                    // Item List
                    WItemListPanel itemList = new WItemListPanel(ChestTracker.CONFIG.visualOptions.columnCount, ChestTracker.CONFIG.visualOptions.rowCount);
                    dimensionPanel.add(itemList, SIDE_PADDING + LEFT_ADDITIONAL_PADDING + BEVEL_PADDING, TOP_PADDING + BEVEL_PADDING, 18 * ChestTracker.CONFIG.visualOptions.columnCount, 18 * ChestTracker.CONFIG.visualOptions.rowCount);

                    // Title
                    dimensionPanel.add(new WLabel(new TranslatableText("chesttracker.gui.title")), LEFT_ADDITIONAL_PADDING + BEVEL_PADDING, BEVEL_PADDING);

                    // Search Field
                    WUpdatableTextField searchField = new WUpdatableTextField(new TranslatableText("chesttracker.gui.search_field_start"));
                    searchField.setOnTextChanged(itemList::setFilter);
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
                        dimensionPanel.add(resetButton, BEVEL_PADDING, height - 26, width - BEVEL_PADDING * 2, 20);
                        resetButton.setOnClick(() -> {
                            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            database.clearDimension(id);
                            itemList.setItems(Collections.emptyList());
                        });
                    }
                }

                WPlainPanel settingsPanel = new WPlainPanel();
                settingsPanel.setSize(width, height);
                tabPanel.add(new WTabPanel.Tab(null, new TextureIcon(id("textures/icon.png")), settingsPanel, builder -> builder.add(new TranslatableText("chesttracker.gui.settings"))));

                WLabel addNewChestsToggleLabel = new WLabel(new TranslatableText("chesttracker.gui.settings.remember_chests"));
                settingsPanel.add(addNewChestsToggleLabel, BEVEL_PADDING, BEVEL_PADDING);
                WToggleButton addNewChestsToggle = new WToggleButton();
                settingsPanel.add(addNewChestsToggle, width - BEVEL_PADDING - 17, 1);
                addNewChestsToggle.setToggle(ChestTracker.CONFIG.miscOptions.rememberNewChests);
                addNewChestsToggle.setOnToggle(newValue -> ChestTracker.CONFIG.miscOptions.rememberNewChests = newValue);

                //noinspection ConstantConditions
                ((AccessorWTabPanel) tabPanel).getMainPanel().setSelectedIndex(selectedTabIndex);
            }


            // Database name
            /*WLabel databaseName = new WLabel(new LiteralText(database == null ? "no database loaded" : database.getId()));
            itemListPanel.add(databaseName, BEVEL_PADDING + LEFT_ADDITIONAL_PADDING, height - 4, 80, 12);

            itemListPanel.validate(this);*/
        }
    }
}
