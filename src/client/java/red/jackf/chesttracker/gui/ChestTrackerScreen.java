package red.jackf.chesttracker.gui;

import com.blamejared.searchables.api.SearchablesConstants;
import com.blamejared.searchables.api.autcomplete.AutoCompletingEditBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.gui.widget.ItemListWidget;
import red.jackf.chesttracker.gui.widget.ResizeWidget;
import red.jackf.chesttracker.memory.ItemMemory;
import red.jackf.chesttracker.memory.LightweightStack;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChestTrackerScreen extends Screen {
    private static final Component TITLE = Component.translatable("chesttracker.title");
    private static final int SMALL_MENU_WIDTH = 176;
    private static final int SMALL_MENU_HEIGHT = 153;
    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 8;
    private static final int GRID_LEFT = 7;
    private static final int GRID_TOP = 38;

    private static final NinePatcher BACKGROUND = new NinePatcher(Constants.TEXTURE, 0, 0, 8, 1);

    private int left = 0;
    private int top = 0;

    // borrowed from creative screen, pressing `t` to focus search also triggers an input on charTyped
    private boolean ignoreTextInput = false;
    private final Screen parent;
    private AutoCompletingEditBox<ItemStack> search;
    private ItemListWidget itemList;
    private List<ItemStack> items = Collections.emptyList();
    @Nullable
    private ResizeWidget resize = null;
    private int menuWidth;
    private int menuHeight;

    public ChestTrackerScreen(@Nullable Screen parent) {
        super(TITLE);
        this.parent = parent;
        ChestTracker.LOGGER.debug("Open Screen");
    }

    @Override
    protected void init() {
        var config = ChestTrackerConfig.INSTANCE.getConfig();
        var liveGridWidth = config.gui.gridWidth + 1;
        var liveGridHeight = config.gui.gridHeight + 1;
        do
            this.menuWidth = SMALL_MENU_WIDTH + (--liveGridWidth - 9) * Constants.SLOT_SIZE;
        while (this.menuWidth > width && liveGridWidth > Constants.MIN_GRID_WIDTH);
        do
            this.menuHeight = SMALL_MENU_HEIGHT + (--liveGridHeight - 6) * Constants.SLOT_SIZE;
        while (this.menuHeight > height && liveGridHeight > Constants.MIN_GRID_HEIGHT);


        this.left = (this.width - menuWidth) / 2;
        this.top = (this.height - menuHeight) / 2;

        super.init();

        this.itemList = this.addRenderableWidget(new ItemListWidget(left + GRID_LEFT, top + GRID_TOP, liveGridWidth, liveGridHeight));
        this.items = getItems();
        this.search = addRenderableWidget(new AutoCompletingEditBox<>(
                font,
                left + 8,
                top + 20,
                menuWidth - 16,
                12,
                search,
                SearchablesConstants.COMPONENT_SEARCH,
                SearchablesUtil.ITEM_STACK,
                () -> items
        ));
        this.addRenderableOnly(this.search.autoComplete());
        this.search.addResponder(this::filter);
        this.search.setValue("");
        this.setInitialFocus(search);

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
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
    }

    private List<ItemStack> getItems() {
        var level = Minecraft.getInstance().level;
        if (level == null) return Collections.emptyList();
        var counts = ItemMemory.INSTANCE.getCounts(level.dimension());
        return counts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<LightweightStack, Integer>>comparingInt(Map.Entry::getValue).reversed()) // sort highest to lowest
                .map(e -> { // lightweight stack -> full stacks
                    var stack = new ItemStack(e.getKey().item());
                    stack.setTag(e.getKey().tag());
                    stack.setCount(e.getValue());
                    return stack;
                }).collect(Collectors.toList());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.search.isFocused() && this.search.autoComplete().mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void filter(String filter) {
        this.itemList.setItems(SearchablesUtil.ITEM_STACK.filterEntries(this.items, filter));
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
        if (search.autoComplete().mouseScrolled(mouseX, mouseY, delta)) {
            return true;
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
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
        this.renderBackground(graphics); // background darken
        BACKGROUND.draw(graphics, left, top, menuWidth, menuHeight);
        super.render(graphics, mouseX, mouseY, tickDelta); // widgets
        graphics.drawString(this.font, this.title, left + TITLE_X, top + TITLE_Y, 0x404040, false); // title
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
