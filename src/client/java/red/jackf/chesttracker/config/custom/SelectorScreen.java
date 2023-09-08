package red.jackf.chesttracker.config.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.gui.widget.ItemButton;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Allows the user to select from a list of options, using ItemStacks as icons.
 * Recommended to pass in a linked map structure such as {@link java.util.LinkedHashMap} for display ordering.
 * @param <T> Type to be selected from
 */
public class SelectorScreen<T> extends Screen {
    private static final int PADDING = 8;
    private static final int CLOSE_BUTTON_SIZE = 20;
    private static final int SEARCH_TOP = 20;
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final int COLUMNS = 13;
    private static final int ROWS = 8;
    private final Screen parent;
    private final Consumer<@Nullable T> consumer;
    private final Map<T, ItemStack> options;
    private Map<T, ItemStack> filteredOptions;
    private EditBox search;
    private int left = 0;
    private int top = 0;

    protected SelectorScreen(Screen parent, Map<T, ItemStack> options, Consumer<@Nullable T> consumer) {
        super(Component.translatable("chesttracker.config.selectorScreen"));
        this.parent = parent;
        this.consumer = consumer;

        this.options = options;

        this.filteredOptions = new LinkedHashMap<>(this.options);
    }

    @Override
    protected void init() {
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;

        this.search = this.addRenderableWidget(new EditBox(Minecraft.getInstance().font,
                this.left + PADDING,
                this.top + SEARCH_TOP,
                WIDTH - 3 * PADDING - CLOSE_BUTTON_SIZE,
                16,
                this.search,
                CommonComponents.EMPTY));
        this.search.setResponder(s -> {
            this.filteredOptions = this.options.entrySet().stream()
                    .filter(entry -> entry.getValue().getHoverName().getString().toLowerCase().contains(s))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
            rebuildWidgets();
        });
        this.search.setCanLoseFocus(false);

        this.setInitialFocus(this.search);

        this.addRenderableWidget(Button.builder(Component.literal("❌"), b -> this.onClose())
                .tooltip(Tooltip.create(CommonComponents.GUI_CANCEL))
                .size(CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE)
                .pos(this.left + WIDTH - CLOSE_BUTTON_SIZE - PADDING, this.top + SEARCH_TOP - 2)
                .build());

        setupItems();
    }

    private void setupItems() {
        final int spacing = 4;
        final int xOffset = 5;

        var iterator = this.filteredOptions.entrySet().iterator();

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if (!iterator.hasNext()) return;
                var option = iterator.next();
                this.addRenderableWidget(new ItemButton(option.getValue(),
                        this.left + xOffset + column * (ItemButton.SIZE + spacing),
                        this.top + 40 + row * (ItemButton.SIZE + spacing),
                        option.getValue().getHoverName(), b -> {
                    SelectorScreen.this.consumer.accept(option.getKey());
                    this.onClose();
                }, false, 0, false));
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics); // background darken
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(this.font, this.title, this.left + PADDING, this.top + PADDING, 0xFF_FFFFFF, true); // title
    }

    @Override
    public void onClose() {
        consumer.accept(null);
        Minecraft.getInstance().setScreen(parent);
    }
}
