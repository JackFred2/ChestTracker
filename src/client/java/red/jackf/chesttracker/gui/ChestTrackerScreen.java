package red.jackf.chesttracker.gui;

import com.blamejared.searchables.api.SearchablesConstants;
import com.blamejared.searchables.api.autcomplete.AutoCompletingEditBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.memory.ItemMemory;
import red.jackf.chesttracker.memory.LightweightStack;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChestTrackerScreen extends Screen {
    private static final Component TITLE = Component.translatable("chesttracker.title");
    private int menuWidth = 176;
    private int menuHeight = 153;
    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 8;
    private static final int GRID_LEFT = 8;
    private static final int GRID_TOP = 39;
    private int gridWidth = 9;
    private int gridHeight = 6;
    private static final int SLOT_SIZE = 18;

    private static final NinePatcher BACKGROUND = new NinePatcher(ChestTracker.id("textures/gui/main_gui.png"), 0, 0, 8, 1);

    private int left = 0;
    private int top = 0;

    // borrowed from creative screen, pressing `t` to focus search also triggers an input on charTyped
    private boolean ignoreTextInput = false;
    private final Screen parent;
    private AutoCompletingEditBox<ItemStack> search;
    private List<ItemStack> items = Collections.emptyList();
    private List<ItemStack> filteredItems = Collections.emptyList();

    public ChestTrackerScreen(@Nullable Screen parent) {
        super(TITLE);
        this.parent = parent;
        ChestTracker.LOGGER.debug("Open Screen");
    }

    @Override
    protected void init() {
        left = (this.width - menuWidth) / 2;
        top = (this.height - menuHeight) / 2;

        super.init();
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
    }

    private List<ItemStack> getItems() {
        var level = Minecraft.getInstance().level;
        if (level == null) return Collections.emptyList();
        var counts = ItemMemory.INSTANCE.getCounts(level.dimension());
        return counts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<LightweightStack, Integer>>comparingInt(Map.Entry::getValue).reversed())
                .map(e -> {
                    var stack = new ItemStack(e.getKey().item());
                    stack.setTag(e.getKey().tag());
                    stack.setCount(e.getValue());
                    return stack;
                }).collect(Collectors.toList());
    }

    private void filter(String filter) {
        this.filteredItems = SearchablesUtil.ITEM_STACK.filterEntries(this.items, filter);
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
        if(search.autoComplete().mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
        this.renderBackground(graphics); // background darken
        BACKGROUND.draw(graphics, left, top, menuWidth, menuHeight);
        super.render(graphics, mouseX, mouseY, tickDelta); // widgets
        graphics.drawString(this.font, this.title, left + TITLE_X, top + TITLE_Y, 0x404040, false); // title
        this.drawItems(graphics); // item list
        this.renderTooltips(graphics, mouseX, mouseY); // tooltips
    }

    private void renderTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (autocompleteHovered(mouseX, mouseY)) return;
        var gridLeft = left + GRID_LEFT - 1; // 1px adjustment to properly align with slot borders
        var gridTop = top + GRID_TOP - 1;

        int column = Math.floorDiv(mouseX - gridLeft, SLOT_SIZE);
        int row = Math.floorDiv(mouseY - gridTop, SLOT_SIZE);
        if (column < gridWidth && row < gridHeight) {
            int index = column + (row * gridWidth);
            if (index < filteredItems.size() && index >= 0) {
                var item = filteredItems.get(index);
                graphics.renderTooltip(this.font, item, mouseX, mouseY);
            }
        }
    }

    private boolean autocompleteHovered(int mouseX, int mouseY) {
        return this.search.autoComplete().isMouseOver(mouseX, mouseY) && this.search.isFocused();
    }

    private void drawItems(@NotNull GuiGraphics graphics) {
        for (int i = 0; i < this.filteredItems.size() && i < (gridWidth * gridHeight); i++) {
            var item = this.filteredItems.get(i);
            graphics.renderItem(item, left + GRID_LEFT + SLOT_SIZE * (i % gridWidth), top + GRID_TOP + SLOT_SIZE * (i / gridWidth));

            graphics.renderItemDecorations(this.font, item, left + GRID_LEFT + SLOT_SIZE * (i % gridWidth), top + GRID_TOP + SLOT_SIZE * (i / gridWidth));
        }
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
