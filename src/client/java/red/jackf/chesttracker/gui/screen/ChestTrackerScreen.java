package red.jackf.chesttracker.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.compat.Compatibility;
import red.jackf.chesttracker.compat.mods.searchables.SearchablesUtil;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.config.ChestTrackerConfigScreenBuilder;
import red.jackf.chesttracker.gui.GuiConstants;
import red.jackf.chesttracker.gui.util.TextColours;
import red.jackf.chesttracker.gui.widget.*;
import red.jackf.chesttracker.memory.Memory;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.provider.ProviderHandler;
import red.jackf.chesttracker.util.Enums;
import red.jackf.chesttracker.util.GuiUtil;
import red.jackf.chesttracker.util.ItemStackUtil;
import red.jackf.chesttracker.util.StreamUtil;

import java.util.*;
import java.util.function.Predicate;

import static net.minecraft.network.chat.Component.translatable;

/**
 * The main screen
 */
public class ChestTrackerScreen extends Screen {
    private static final Component TITLE = translatable("chesttracker.title");
    private static final int TITLE_LEFT = 8;
    private static final int TITLE_TOP = 8;
    private static final int SEARCH_LEFT = 8;
    private static final int SEARCH_TOP = 27;
    private static final int GRID_LEFT = 7;
    private static final int GRID_TOP = 41;
    private static final int BUTTON_SIZE = 14;
    private static final int MEMORY_ICON_OFFSET = 24;
    private static final int MEMORY_ICON_SPACING = 24;
    private static final int SMALL_MENU_WIDTH = 192;
    private static final int SMALL_MENU_HEIGHT = 156;

    private static ContainerFilter containerFilter = ContainerFilter.ALL;
    private static ItemSort itemSort = ItemSort.COUNT_DESCENDING;

    private int left = 0;
    private int top = 0;
    private int menuWidth;
    private int menuHeight;

    // borrowed from creative screen, pressing `t` to focus search also triggers an input on charTyped
    private boolean ignoreTextInput = false;
    private final Screen parent;
    private EditBox search;
    private ItemListWidget itemList;
    @Nullable
    private ResizeWidget resize = null;
    private VerticalScrollWidget scroll;
    private ResourceLocation currentMemoryKey;
    private List<ItemStack> items = Collections.emptyList();

    public ChestTrackerScreen(@Nullable Screen parent) {
        super(TITLE);
        ChestTracker.LOGGER.debug("Open Screen");
        this.parent = parent;
        var currentKey = ProviderHandler.getCurrentKey();
        this.currentMemoryKey = currentKey == null ? ChestTracker.id("unknown") : currentKey;
    }

    @Override
    protected void init() {
        // ask for a memory to be loaded if not available
        if (MemoryBank.INSTANCE == null) {
            Minecraft.getInstance()
                     .setScreen(new MemoryBankManagerScreen(parent, () -> Minecraft.getInstance().setScreen(this)));
            return;
        }

        var config = ChestTrackerConfig.INSTANCE.instance();
        var liveGridWidth = config.gui.gridWidth + 1;
        var liveGridHeight = config.gui.gridHeight + 1;

        // shrink until fits on screen
        do
            this.menuWidth = SMALL_MENU_WIDTH + (--liveGridWidth - 9) * GuiConstants.GRID_SLOT_SIZE;
        while (this.menuWidth > width && liveGridWidth > GuiConstants.MIN_GRID_COLUMNS);
        do
            this.menuHeight = SMALL_MENU_HEIGHT + (--liveGridHeight - 6) * GuiConstants.GRID_SLOT_SIZE;
        while (this.menuHeight > height && liveGridHeight > GuiConstants.MIN_GRID_ROWS);

        this.left = (this.width - menuWidth) / 2;
        this.top = (this.height - menuHeight) / 2;

        super.init();

        // items
        this.itemList = new ItemListWidget(left + GRID_LEFT, top + GRID_TOP, liveGridWidth, liveGridHeight);

        // scroll
        this.scroll = this.addRenderableWidget(new VerticalScrollWidget(left + menuWidth - 19, top + GRID_TOP, this.itemList.getHeight(), Component.empty()));
        this.scroll.setResponder(this.itemList::onScroll);

        this.addRenderableWidget(this.itemList);

        // search
        var shouldFocusSearch = this.search == null || this.search.isFocused();
        shouldFocusSearch &= config.gui.autofocusSearchBar;
        if (config.gui.showAutocomplete && Compatibility.SEARCHABLES) {
            this.search = addRenderableWidget(SearchablesUtil.getEditBox(
                    font,
                    left + SEARCH_LEFT,
                    top + SEARCH_TOP,
                    menuWidth - 16,
                    12,
                    this.search,
                    () -> items,
                    this::filter
            ));
        } else {
            this.search = addRenderableWidget(new CustomEditBox(
                    font,
                    left + SEARCH_LEFT,
                    top + SEARCH_TOP,
                    menuWidth - 16,
                    12,
                    this.search,
                    CustomEditBox.SEARCH_MESSAGE
            ));
            this.search.setHint(CustomEditBox.SEARCH_MESSAGE);
            this.search.setResponder(this::filter);
        }
        this.search.setTextColor(TextColours.getTextColour());
        this.search.setBordered(false);
        this.search.setValue(this.search.getValue());
        ifSearchables(() -> this.addRenderableWidget(SearchablesUtil.getWrappedAutocomplete(this.search)));

        if (shouldFocusSearch)
            this.setInitialFocus(search);

        // close
        this.addRenderableWidget(GuiUtil.close(
                    this.left + this.menuWidth - (3 + BUTTON_SIZE),
                    this.top + GuiConstants.SMALL_MARGIN,
                    button -> this.onClose()));

        // mod settings
        this.addRenderableWidget(new ImageButton(
                    this.left + this.menuWidth - 2 * (3 + BUTTON_SIZE),
                    this.top + GuiConstants.SMALL_MARGIN,
                    BUTTON_SIZE,
                    BUTTON_SIZE,
                    GuiUtil.twoSprite("mod_settings/button"),
                    button -> Minecraft.getInstance().setScreen(ChestTrackerConfigScreenBuilder.build(this))))
            .setTooltip(Tooltip.create(translatable("chesttracker.gui.modSettings")));

        // change memory bank
        this.addRenderableWidget(new ImageButton(
                    this.left + this.menuWidth - 3 * (3 + BUTTON_SIZE),
                    this.top + GuiConstants.SMALL_MARGIN,
                    BUTTON_SIZE,
                    BUTTON_SIZE,
                    GuiUtil.twoSprite("change_memory_bank/button"),
                    this::openMemoryManager))
            .setTooltip(Tooltip.create(translatable("chesttracker.gui.openMemoryManager")));

        // memory bank settings
        this.addRenderableWidget(new ImageButton(
                    this.left + this.menuWidth - 4 * (3 + BUTTON_SIZE),
                    this.top + GuiConstants.SMALL_MARGIN,
                    BUTTON_SIZE,
                    BUTTON_SIZE,
                    GuiUtil.twoSprite("memory_bank_settings/button"),
                    this::openMemoryBankSettings))
            .setTooltip(Tooltip.create(translatable("chesttracker.gui.memoryBankSettings")));

        // filtering
        this.addRenderableWidget(new ChangeableImageButton(
                    this.left + this.menuWidth - 5 * (3 + BUTTON_SIZE),
                    this.top + GuiConstants.SMALL_MARGIN,
                    BUTTON_SIZE,
                    BUTTON_SIZE,
                    containerFilter.sprites,
                    CommonComponents.EMPTY,
                    this::cycleContainerFilter))
            .setTooltip(this.getContainerFilterTooltip());

        // item sort
        this.addRenderableWidget(new ChangeableImageButton(
                    this.left + this.menuWidth - 6 * (3 + BUTTON_SIZE),
                    this.top + GuiConstants.SMALL_MARGIN,
                    BUTTON_SIZE,
                    BUTTON_SIZE,
                    itemSort.sprites,
                    CommonComponents.EMPTY,
                    this::cycleItemSort))
            .setTooltip(this.getItemSortTooltip());

        // resize
        if (config.gui.showResizeWidget)
            this.resize = this.addRenderableWidget(new ResizeWidget(left + menuWidth - 10, top + menuHeight - 10, left, top,
                                                                    GuiConstants.GRID_SLOT_SIZE, config.gui.gridWidth, config.gui.gridHeight,
                                                                    GuiConstants.MIN_GRID_COLUMNS, GuiConstants.MIN_GRID_ROWS, GuiConstants.MAX_GRID_WIDTH, GuiConstants.MAX_GRID_HEIGHT, (w, h) -> {
                ChestTracker.LOGGER.debug("Resizing to {}w, {}h", w, h);
                ChestTrackerConfig.INSTANCE.instance().gui.gridWidth = w;
                ChestTrackerConfig.INSTANCE.instance().gui.gridHeight = h;
                ChestTrackerConfig.INSTANCE.save();
                rebuildWidgets();
            }));

        // key buttons
        if (MemoryBank.INSTANCE != null) {
            // fix bad order on first open of screen, kind of hacky
            MemoryBank.INSTANCE.getKeys().forEach(loc -> MemoryBank.INSTANCE.getMetadata().getVisualSettings()
                                                                            .getOrCreateIcon(loc));

            var todo = MemoryBank.INSTANCE.getKeys().stream()
                                          .sorted(StreamUtil.bringToFront(MemoryBank.INSTANCE.getMetadata()
                                                                                             .getVisualSettings()
                                                                                             .getKeyOrder())).toList();
            Map<ResourceLocation, ItemButton> buttons = new HashMap<>();

            for (int index = 0; index < todo.size(); index++) {
                var resloc = todo.get(index);

                // get the relevant icon
                var icon = MemoryBank.INSTANCE.getMetadata().getVisualSettings().getOrCreateIcon(resloc);
                var button = this.addRenderableWidget(new ItemButton(icon,
                                                                     this.left - MEMORY_ICON_OFFSET,
                                                                     this.top + index * MEMORY_ICON_SPACING, b -> {
                    // unhighlight old
                    if (buttons.containsKey(this.currentMemoryKey))
                        buttons.get(this.currentMemoryKey).setHighlighted(false);

                    // set item list
                    this.currentMemoryKey = resloc;
                    updateItems();

                    // highlight new
                    buttons.get(resloc).setHighlighted(true);
                }, ItemButton.Background.CUSTOM));

                button.setTooltip(Tooltip.create(Component.literal(resloc.toString())));

                buttons.put(resloc, button);

                // inital button highlight
                if (currentMemoryKey.equals(resloc)) button.setHighlighted(true);
            }
        }

        updateItems();

        // add warning, this should hopefully never be displayed
        if (MemoryBank.INSTANCE == null) {
            this.search.setEditable(false);
            this.search.setValue(I18n.get("chesttracker.config.memory.global.noMemoryBankLoaded"));
            this.search.setTextColorUneditable(0xFF4040);
        }
    }

    private void cycleItemSort(ChangeableImageButton button) {
        itemSort = Enums.next(itemSort);
        button.setTooltip(getItemSortTooltip());
        button.setSprites(itemSort.sprites);
        updateItems();
    }

    private Tooltip getItemSortTooltip() {
        return Tooltip.create(translatable("chesttracker.gui.itemSort", itemSort.tooltip));
    }

    private Tooltip getContainerFilterTooltip() {
        return Tooltip.create(translatable("chesttracker.gui.containerFilter", containerFilter.tooltip));
    }

    private void cycleContainerFilter(ChangeableImageButton button) {
        containerFilter = Enums.next(containerFilter);
        button.setTooltip(getContainerFilterTooltip());
        button.setSprites(containerFilter.sprites);
        updateItems();
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
    }

    /**
     * Update the cached item list from the current Memory Bank, then runs a filter operation..
     */
    private void updateItems() {
        if (MemoryBank.INSTANCE == null) return;
        int maxRange = MemoryBank.INSTANCE.getMetadata().getSearchSettings().itemListRange;

        Predicate<Map.Entry<BlockPos, Memory>> predicate = getItemListFilter(maxRange);

        this.items = MemoryBank.INSTANCE.getCounts(currentMemoryKey, predicate, MemoryBank.CountMergeMode.WITHIN_CONTAINERS)
                .stream()
                .sorted(itemSort.sort)
                .toList();

        filter(this.search.getValue());
    }

    private Predicate<Map.Entry<BlockPos, Memory>> getItemListFilter(int maxRange) {
        Predicate<Map.Entry<BlockPos, Memory>> predicate = containerFilter.filter;

        // apply max range if necessary
        if (Minecraft.getInstance().player != null && Objects.equals(ProviderHandler.getCurrentKey(), currentMemoryKey)) {
            long squareMaxRange = (long) maxRange * maxRange;
            Vec3 origin = Minecraft.getInstance().player.getEyePosition();
            predicate = predicate.and(entry -> entry.getKey().getCenter().distanceToSqr(origin) < squareMaxRange);
        }
        return predicate;
    }

    /**
     * Update the items list with the currently cached items, filtered by the search bar.
     */
    private void filter(String filter) {
        List<ItemStack> filtered;
        if (Compatibility.SEARCHABLES) {
            filtered = SearchablesUtil.ITEM_STACK.filterEntries(this.items, filter.toLowerCase());
        } else {
            filtered = this.items.stream().filter(stack -> ItemStackUtil.defaultPredicate(stack, filter.toLowerCase())).toList();
        }
        this.itemList.setItems(filtered);
        var guiConfig = ChestTrackerConfig.INSTANCE.instance().gui;
        this.scroll.setDisabled(filtered.size() <= (guiConfig.gridWidth * guiConfig.gridHeight));
    }

    /**
     * Test for autocomplete-specific operations. Used to check mouse operations over the autocomplete bar.
     */
    private boolean ifSearchables(Predicate<AbstractWidget> predicate) {
        return Compatibility.SEARCHABLES && SearchablesUtil.ifSearchables(this.search, predicate);
    }

    private void ifSearchables(Runnable ifSearchables) {
        ifSearchables(ignored -> {
            ifSearchables.run();
            return false;
        });
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
        this.itemList.setHideTooltip(this.search.isFocused() && ifSearchables(a -> a.isMouseOver(mouseX, mouseY)));
        super.render(graphics, mouseX, mouseY, tickDelta); // widgets
        graphics.drawString(this.font, this.title, left + TITLE_LEFT, top + TITLE_TOP, TextColours.getLabelColour(), false); // title
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int i, int j, float f) {
        super.renderBackground(graphics, i, j, f);
        graphics.blitSprite(GuiUtil.BACKGROUND_SPRITE, left, top, menuWidth, menuHeight);
        ifSearchables(() -> graphics.blitSprite(GuiUtil.SEARCH_BAR_SPRITE, search.getX() - 2, search.getY() - 2, search.getWidth() + 4, search.getHeight()));
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (ignoreTextInput) {
            return false;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.ignoreTextInput = false;
        if (this.getFocused() == search) {
            if (keyCode == GLFW.GLFW_KEY_TAB) {
                this.setFocused(null);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.search.isFocused() && ifSearchables(a -> a.mouseClicked(mouseX, mouseY, button))) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        // Searchables Edit Box Support
        double finalDelta = deltaY;
        if (search.isFocused() && ifSearchables(a -> a.mouseScrolled(mouseX, mouseY, deltaX, finalDelta))) {
            return true;
        } else if (itemList.isMouseOver(mouseX, mouseY) || scroll.isMouseOver(mouseX, mouseY)) {
            deltaY /= Math.max(1, itemList.getRows() - ChestTrackerConfig.INSTANCE.instance().gui.gridHeight);
            return scroll.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (resize != null && resize.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void openMemoryManager(Button ignored) {
        Minecraft.getInstance().setScreen(new MemoryBankManagerScreen(
                () -> MemoryBank.INSTANCE == null ? parent : this,
                // return to this screen unless the memories have been unloaded, in which case go to the parent
                () -> Minecraft.getInstance().setScreen(this)
        ));
    }

    private void openMemoryBankSettings(Button button) {
        if (MemoryBank.INSTANCE == null) return;
        Minecraft.getInstance().setScreen(new EditMemoryBankScreen(
                this,
                this::updateItems,
                MemoryBank.INSTANCE.getId()
        ));
    }

    public enum ContainerFilter {
        ALL(GuiUtil.twoSprite("container_filter/all"),
            translatable("chesttracker.gui.containerFilter.all"),
            memory -> true),
        CHESTS(GuiUtil.twoSprite("container_filter/chests"),
               translatable("chesttracker.gui.containerFilter.chests"),
               memory -> memory.getValue().container().map(b -> b instanceof AbstractChestBlock<?>).orElse(false)),
        BARRELS(GuiUtil.twoSprite("container_filter/barrels"),
                translatable("chesttracker.gui.containerFilter.barrels"),
                memory -> memory.getValue().container().map(b -> b instanceof BarrelBlock).orElse(false)),
        SHULKER_BOXES(GuiUtil.twoSprite("container_filter/shulker_boxes"),
                      translatable("chesttracker.gui.containerFilter.shulkerBoxes"),
                      memory -> memory.getValue().container().map(b -> b instanceof ShulkerBoxBlock).orElse(false)),
        HOPPERS(GuiUtil.twoSprite("container_filter/hoppers"),
                translatable("chesttracker.gui.containerFilter.hoppers"),
        memory -> memory.getValue().container().map(b -> b instanceof HopperBlock).orElse(false)),
        FURNACES(GuiUtil.twoSprite("container_filter/furnaces"),
                 translatable("chesttracker.gui.containerFilter.furnaces"),
                 memory -> memory.getValue().container().map(b -> b instanceof AbstractFurnaceBlock).orElse(false));

        private final WidgetSprites sprites;
        private final Component tooltip;
        private final Predicate<Map.Entry<BlockPos, Memory>> filter;

        ContainerFilter(WidgetSprites sprites, Component tooltip, Predicate<Map.Entry<BlockPos, Memory>> filter) {
            this.sprites = sprites;
            this.tooltip = tooltip;
            this.filter = filter;
        }
    }

    public enum ItemSort {
        COUNT_DESCENDING(GuiUtil.twoSprite("item_sort/count_descending"),
                         translatable("chesttracker.gui.itemSort.countDescending"),
                         Comparator.comparingInt(ItemStack::getCount).reversed()),
        COUNT_ASCENDING(GuiUtil.twoSprite("item_sort/count_ascending"),
                         translatable("chesttracker.gui.itemSort.countAscending"),
                         Comparator.comparingInt(ItemStack::getCount)),
        ALPHABETICAL_DESCENDING(GuiUtil.twoSprite("item_sort/alphabetical_descending"),
                                translatable("chesttracker.gui.itemSort.alphabeticalDescending"),
                                Comparator.comparing(stack -> stack.getDisplayName().getString().toLowerCase(Locale.ROOT))),
        ALPHABETICAL_ASCENDING(GuiUtil.twoSprite("item_sort/alphabetical_ascending"),
                                translatable("chesttracker.gui.itemSort.alphabeticalAscending"),
                                Comparator.<ItemStack, String>comparing(stack -> stack.getDisplayName().getString().toLowerCase(Locale.ROOT)).reversed());

        private final WidgetSprites sprites;
        private final Component tooltip;
        private final Comparator<ItemStack> sort;

        ItemSort(WidgetSprites sprites, Component tooltip, Comparator<ItemStack> sort) {
            this.sprites = sprites;
            this.tooltip = tooltip;
            this.sort = sort;
        }
    }
}
