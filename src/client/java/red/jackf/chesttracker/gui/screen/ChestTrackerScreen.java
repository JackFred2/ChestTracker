package red.jackf.chesttracker.gui.screen;

import com.blamejared.searchables.api.SearchablesConstants;
import com.blamejared.searchables.api.autcomplete.AutoComplete;
import com.blamejared.searchables.api.autcomplete.AutoCompletingEditBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.config.ChestTrackerConfigScreenBuilder;
import red.jackf.chesttracker.gui.util.CustomSearchablesFormatter;
import red.jackf.chesttracker.gui.util.NinePatcher;
import red.jackf.chesttracker.gui.util.SearchablesUtil;
import red.jackf.chesttracker.gui.util.TextColours;
import red.jackf.chesttracker.gui.widget.*;
import red.jackf.chesttracker.memory.LightweightStack;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.util.Constants;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The main screen
 */
public class ChestTrackerScreen extends Screen {
    private static final Component TITLE = Component.translatable("chesttracker.title");
    private static final int TITLE_LEFT = 8;
    private static final int TITLE_TOP = 8;
    private static final int SEARCH_LEFT = 8;
    private static final int SEARCH_TOP = 27;
    private static final int GRID_LEFT = 7;
    private static final int GRID_TOP = 41;
    private static final int BUTTON_SIZE = 14;
    private static final int BUTTON_TOP = 7;
    private static final int SETTINGS_RIGHT = 6;
    private static final int CHANGE_MEMORY_RIGHT = 26;
    private static final int MEMORY_ICON_OFFSET = 24;
    private static final int MEMORY_ICON_SPACING = 24;
    private static final int SMALL_MENU_WIDTH = 192;
    private static final int SMALL_MENU_HEIGHT = 156;

    private int left = 0;
    private int top = 0;

    // borrowed from creative screen, pressing `t` to focus search also triggers an input on charTyped
    private boolean ignoreTextInput = false;
    private final Screen parent;
    private EditBox search;
    private ItemListWidget itemList;
    @Nullable
    private ResizeWidget resize = null;
    private VerticalScrollWidget scroll;
    private ResourceLocation memoryKey;
    private List<ItemStack> items = Collections.emptyList();
    private int menuWidth;
    private int menuHeight;

    public ChestTrackerScreen(@Nullable Screen parent) {
        super(TITLE);
        ChestTracker.LOGGER.debug("Open Screen");
        this.parent = parent;
        var level = Minecraft.getInstance().level;
        this.memoryKey = level == null ? ChestTracker.id("unknown") : level.dimension().location();
    }

    @Override
    protected void init() {
        // ask for a memory to be loaded if not available
        if (MemoryBank.INSTANCE == null) {
            Minecraft.getInstance().setScreen(new MemoryBankManagerScreen(parent, () -> Minecraft.getInstance().setScreen(this)));
            return;
        }

        var config = ChestTrackerConfig.INSTANCE.getConfig();
        var liveGridWidth = config.gui.gridWidth + 1;
        var liveGridHeight = config.gui.gridHeight + 1;

        // shrink until fits on screen
        do
            this.menuWidth = SMALL_MENU_WIDTH + (--liveGridWidth - 9) * Constants.SLOT_SIZE;
        while (this.menuWidth > width && liveGridWidth > Constants.MIN_GRID_WIDTH);
        do
            this.menuHeight = SMALL_MENU_HEIGHT + (--liveGridHeight - 6) * Constants.SLOT_SIZE;
        while (this.menuHeight > height && liveGridHeight > Constants.MIN_GRID_HEIGHT);

        // resize so background ninepatcher looks nice
        this.menuWidth = NinePatcher.BACKGROUND.fitsNicely(this.menuWidth);
        this.menuHeight = NinePatcher.BACKGROUND.fitsNicely(this.menuHeight);

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
        var formatter = new CustomSearchablesFormatter(SearchablesUtil.ITEM_STACK);
        if (config.gui.showAutocomplete) {
            var autocompleting = addRenderableWidget(new AutoCompletingEditBox<>(
                    font,
                    left + SEARCH_LEFT,
                    top + SEARCH_TOP,
                    menuWidth - 16,
                    12,
                    this.search,
                    SearchablesConstants.COMPONENT_SEARCH,
                    SearchablesUtil.ITEM_STACK,
                    () -> items
            ));
            autocompleting.setFormatter(formatter);
            autocompleting.addResponder(formatter);
            autocompleting.addResponder(this::filter);
            this.search = autocompleting;
        } else {
            this.search = addRenderableWidget(new CustomEditBox(
                    font,
                    left + SEARCH_LEFT,
                    top + SEARCH_TOP,
                    menuWidth - 16,
                    12,
                    this.search,
                    SearchablesConstants.COMPONENT_SEARCH
            ));
            this.search.setFormatter(formatter);
            this.search.setHint(SearchablesConstants.COMPONENT_SEARCH);
            this.search.setResponder(s -> {
                formatter.accept(s);
                this.filter(s);
            });
        }
        this.search.setTextColor(TextColours.getSearchTextColour());
        this.search.setBordered(false);
        this.search.setValue(this.search.getValue());
        if (this.search instanceof AutoCompletingEditBox<?> autoCompleting)
            this.addRenderableOnly(autoCompleting.autoComplete());

        if (shouldFocusSearch)
            this.setInitialFocus(search);

        // settings
        var settingsButton = this.addRenderableWidget(new ImageButton(
                left + menuWidth - SETTINGS_RIGHT - BUTTON_SIZE,
                top + BUTTON_TOP,
                BUTTON_SIZE,
                BUTTON_SIZE,
                0,
                0,
                BUTTON_SIZE,
                ChestTracker.guiTex("widgets/settings_button"),
                BUTTON_SIZE,
                BUTTON_SIZE * 2,
                button -> Minecraft.getInstance().setScreen(ChestTrackerConfigScreenBuilder.build(this))));
        settingsButton.setTooltip(Tooltip.create(Component.translatable("mco.configure.world.buttons.settings")));

        // change memories
        this.addRenderableWidget(new ImageButton(
                left + menuWidth - CHANGE_MEMORY_RIGHT - BUTTON_SIZE,
                top + BUTTON_TOP,
                BUTTON_SIZE,
                BUTTON_SIZE,
                0,
                0,
                BUTTON_SIZE,
                ChestTracker.guiTex("widgets/change_memory_bank_button"),
                BUTTON_SIZE,
                BUTTON_SIZE * 2,
                b -> Minecraft.getInstance().setScreen(
                        new MemoryBankManagerScreen(() -> MemoryBank.INSTANCE == null ? parent : this, () ->
                                // return to this screen unless the memories have been unloaded, in which case the parent
                                Minecraft.getInstance().setScreen(this))))).setTooltip(Tooltip.create(Component.translatable("chesttracker.gui.openMemoryManager")));

        // resize
        if (config.gui.showResizeWidget)
            this.resize = this.addRenderableWidget(new ResizeWidget(left + menuWidth - 10, top + menuHeight - 10, left, top,
                    Constants.SLOT_SIZE, config.gui.gridWidth, config.gui.gridHeight,
                    Constants.MIN_GRID_WIDTH, Constants.MIN_GRID_HEIGHT, Constants.MAX_GRID_WIDTH, Constants.MAX_GRID_HEIGHT, (w, h) -> {
                ChestTracker.LOGGER.debug("Resizing to {}w, {}h", w, h);
                ChestTrackerConfig.INSTANCE.getConfig().gui.gridWidth = w;
                ChestTrackerConfig.INSTANCE.getConfig().gui.gridHeight = h;
                ChestTrackerConfig.INSTANCE.save();
                rebuildWidgets();
            }));

        // key buttons
        if (MemoryBank.INSTANCE != null) {
            var iconList = ChestTrackerConfig.INSTANCE.getConfig().gui.memoryKeyIcons;
            var todo = MemoryBank.INSTANCE.getKeys();
            Map<ResourceLocation, ItemButton> buttons = new HashMap<>(); // used to manage highlights

            for (int index = 0; index < todo.size(); index++) {
                var resloc = todo.get(index);
                var icon = iconList.stream()
                        .filter(memoryKeyIcon -> memoryKeyIcon.id().equals(resloc))
                        .findFirst()
                        .map(mi -> mi.icon().toStack())
                        .orElse(new ItemStack(Items.CRAFTING_TABLE));
                var button = this.addRenderableWidget(new ItemButton(icon,
                        this.left - MEMORY_ICON_OFFSET,
                        this.top + index * MEMORY_ICON_SPACING,
                        Component.literal(resloc.toString()), b -> {
                    // unhighlight old
                    if (buttons.containsKey(this.memoryKey))
                        buttons.get(this.memoryKey).setHighlighted(false);

                    // set item list
                    this.memoryKey = resloc;
                    updateItems();

                    // highlight new
                    buttons.get(resloc).setHighlighted(true);
                }, true, 200, true));

                buttons.put(resloc, button);

                // inital button highlight
                if (memoryKey.equals(resloc)) button.setHighlighted(true);
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

    @Override
    protected void repositionElements() {
        super.repositionElements();
    }

    /**
     * Update the cached item list from the current Memory Bank, then runs a filter operation..
     */
    private void updateItems() {
        if (MemoryBank.INSTANCE == null) return;
        var counts = MemoryBank.INSTANCE.getCounts(memoryKey);
        this.items = counts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<LightweightStack, Integer>>comparingInt(Map.Entry::getValue).reversed()) // sort highest to lowest
                .map(e -> { // lightweight stack -> full stacks
                    var stack = new ItemStack(e.getKey().item());
                    stack.setTag(e.getKey().tag());
                    stack.setCount(e.getValue());
                    return stack;
                }).collect(Collectors.toList());
        filter(this.search.getValue());
    }

    /**
     * Update the items list with the currently cached items, filtered by the search bar.
     */
    private void filter(String filter) {
        var filtered = SearchablesUtil.ITEM_STACK.filterEntries(this.items, filter.toLowerCase());
        this.itemList.setItems(filtered);
        var guiConfig = ChestTrackerConfig.INSTANCE.getConfig().gui;
        this.scroll.setDisabled(filtered.size() <= (guiConfig.gridWidth * guiConfig.gridHeight));
    }

    /**
     * Test for autocomplete-specific operations. Used to check mouse operations over the autocomplete bar.
     */
    private boolean ifAutocomplete(Predicate<AutoComplete<?>> predicate) {
        if (this.search instanceof AutoCompletingEditBox<?> autoCompleting)
            return predicate.test(autoCompleting.autoComplete());
        else {
            return false;
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
        this.renderBackground(graphics); // background darken
        NinePatcher.BACKGROUND.draw(graphics, left, top, menuWidth, menuHeight);
        if (this.search instanceof AutoCompletingEditBox<?>)
            NinePatcher.SEARCH.draw(graphics, search.getX() - 2, search.getY() - 2, search.getWidth() + 4, search.getHeight());
        this.itemList.setHideTooltip(this.search.isFocused() && ifAutocomplete(a -> a.isMouseOver(mouseX, mouseY)));
        super.render(graphics, mouseX, mouseY, tickDelta); // widgets
        graphics.drawString(this.font, this.title, left + TITLE_LEFT, top + TITLE_TOP, TextColours.getLabelColour(), false); // title
    }

    @Override
    public void tick() {
        super.tick();
        // Searchables Edit Box Support
        this.search.tick();
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
        if (this.search.isFocused() && ifAutocomplete(a -> a.mouseClicked(mouseX, mouseY, button))) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Searchables Edit Box Support
        double finalDelta = delta;
        if (search.isFocused() && ifAutocomplete(a -> a.mouseScrolled(mouseX, mouseY, finalDelta))) {
            return true;
        } else if (itemList.isMouseOver(mouseX, mouseY) || scroll.isMouseOver(mouseX, mouseY)) {
            delta /= Math.max(1, itemList.getRows() - ChestTrackerConfig.INSTANCE.getConfig().gui.gridHeight);
            return scroll.mouseScrolled(mouseX, mouseY, delta);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
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
}
