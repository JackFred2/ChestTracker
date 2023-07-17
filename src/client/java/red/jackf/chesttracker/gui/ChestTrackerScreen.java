package red.jackf.chesttracker.gui;

import com.blamejared.searchables.api.SearchablesConstants;
import com.blamejared.searchables.api.autcomplete.AutoCompletingEditBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.config.ChestTrackerConfigScreenBuilder;
import red.jackf.chesttracker.gui.widget.ItemListWidget;
import red.jackf.chesttracker.gui.widget.ResizeWidget;
import red.jackf.chesttracker.gui.widget.VerticalScrollWidget;
import red.jackf.chesttracker.memory.ItemMemory;
import red.jackf.chesttracker.memory.LightweightStack;
import red.jackf.chesttracker.util.Constants;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChestTrackerScreen extends Screen {
    private static final Component TITLE = Component.translatable("chesttracker.title");
    private static final int TITLE_LEFT = 8;
    private static final int TITLE_TOP = 8;
    private static final int SEARCH_LEFT = 8;
    private static final int SEARCH_TOP = 24;
    private static final int GRID_LEFT = 7;
    private static final int GRID_TOP = 38;
    private static final int SETTINGS_RIGHT = 6;
    private static final int SETTINGS_TOP = 5;
    private static final int SETTINGS_SIZE = 14;
    private static final int SETTINGS_UV_X = 0;
    private static final int SETTINGS_UV_Y = 86;
    private static final int SMALL_MENU_WIDTH = 192;
    private static final int SMALL_MENU_HEIGHT = 153;

    private static final NinePatcher BACKGROUND = new NinePatcher(Constants.TEXTURE, 0, 0, 8, 1);
    private static final NinePatcher SEARCH = new NinePatcher(Constants.TEXTURE, 0, 28, 4, 1);

    private int left = 0;
    private int top = 0;

    // borrowed from creative screen, pressing `t` to focus search also triggers an input on charTyped
    private boolean ignoreTextInput = false;
    private final Screen parent;
    private AutoCompletingEditBox<ItemStack> search;
    private ItemListWidget itemList;
    @Nullable
    private ResizeWidget resize = null;
    private VerticalScrollWidget scroll;
    private ResourceLocation memoryId;
    private List<ItemStack> items = Collections.emptyList();
    private int menuWidth;
    private int menuHeight;

    public ChestTrackerScreen(@Nullable Screen parent) {
        super(TITLE);
        ChestTracker.LOGGER.debug("Open Screen");
        this.parent = parent;
        var level = Minecraft.getInstance().level;
        this.memoryId = level == null ? ChestTracker.id("unknown") : level.dimension().location();
    }

    @Override
    protected void init() {
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


        this.left = (this.width - menuWidth) / 2;
        this.top = (this.height - menuHeight) / 2;

        super.init();

        // items
        this.itemList = this.addRenderableWidget(new ItemListWidget(left + GRID_LEFT, top + GRID_TOP, liveGridWidth, liveGridHeight));

        // scroll
        this.scroll = this.addRenderableWidget(new VerticalScrollWidget(left + menuWidth - 19, top + GRID_TOP, this.itemList.getHeight(), Component.empty()));
        this.scroll.setResponder(this.itemList::onScroll);

        // search
        var shouldFocusSearch = this.search == null || this.search.isFocused();
        shouldFocusSearch &= config.gui.autofocusSearchBar;
        this.search = addRenderableWidget(new AutoCompletingEditBox<>(
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
        this.search.addResponder(this::filter);
        this.search.setBordered(false);
        this.search.setValue(this.search.getValue());
        this.addRenderableOnly(this.search.autoComplete());

        if (shouldFocusSearch)
            this.setInitialFocus(search);

        // settings
        var settingsButton = this.addRenderableWidget(new ImageButton(left + menuWidth - SETTINGS_RIGHT - SETTINGS_SIZE, top + SETTINGS_TOP,
                SETTINGS_SIZE, SETTINGS_SIZE,
                SETTINGS_UV_X, SETTINGS_UV_Y,
                0, Constants.TEXTURE, 256, 256,
                button -> Minecraft.getInstance().setScreen(ChestTrackerConfigScreenBuilder.build(this)),
                Component.translatable("mco.configure.world.buttons.settings")));
        settingsButton.setTooltip(Tooltip.create(Component.translatable("mco.configure.world.buttons.settings")));

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

        updateItems();
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
    }

    /**
     * Loads the items from the given save and dimension ID
     */
    private void updateItems() {
        if (ItemMemory.INSTANCE == null) return;
        var counts = ItemMemory.INSTANCE.getCounts(memoryId);
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
     * Updates the items list with the current items, filtered by the search bar
     */
    private void filter(String filter) {
        var filtered = SearchablesUtil.ITEM_STACK.filterEntries(this.items, filter);
        this.itemList.setItems(filtered);
        var guiConfig = ChestTrackerConfig.INSTANCE.getConfig().gui;
        this.scroll.setDisabled(filtered.size() <= (guiConfig.gridWidth * guiConfig.gridHeight));
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
        this.renderBackground(graphics); // background darken
        BACKGROUND.draw(graphics, left, top, menuWidth, menuHeight);
        SEARCH.draw(graphics, search.getX() - 2, search.getY() - 2, search.getWidth() + 4, search.getHeight());
        this.itemList.setShouldShowTooltip(!this.search.isFocused() || !this.search.autoComplete().isMouseOver(mouseX, mouseY));
        super.render(graphics, mouseX, mouseY, tickDelta); // widgets
        graphics.drawString(this.font, this.title, left + TITLE_LEFT, top + TITLE_TOP, RenderUtil.titleColour, false); // title
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.search.isFocused() && this.search.autoComplete().mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Searchables Edit Box Support
        if (search.isFocused() && search.autoComplete().mouseScrolled(mouseX, mouseY, delta)) {
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
