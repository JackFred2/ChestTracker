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
    private static final int TOP_BUFFER = 40;
    private static final int HORIZONTAL_PADDING = 20;
    private static final int TOP_PADDING = 10;
    private static final int BOTTOM_PADDING = 20;
    private final Screen parent;
    private final Consumer<@Nullable T> consumer;
    private final Map<T, ItemStack> options;
    private Map<T, ItemStack> filteredOptions;
    private int menuWidth;
    private int menuHeight;
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
        this.menuWidth = this.width - 2 * HORIZONTAL_PADDING;
        this.menuHeight = this.height - TOP_PADDING - BOTTOM_PADDING;

        this.left = (this.width - menuWidth) / 2;
        this.top = (this.height - menuHeight) / 2;

        this.search = this.addRenderableWidget(new EditBox(Minecraft.getInstance().font,
                this.left,
                this.top + 20,
                menuWidth,
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
                .size(20, 20)
                .pos(this.left + this.menuWidth - 20, 8)
                .build());

        setupItems();
    }

    private void setupItems() {
        final int spacing = 24;

        var iterator = this.filteredOptions.entrySet().iterator();

        for (int y = 0; y < menuHeight - TOP_BUFFER; y += spacing) {
            for (int x = 0; x < menuWidth; x += spacing) {
                if (!iterator.hasNext()) return;
                var option = iterator.next();
                this.addRenderableWidget(new ItemButton(option.getValue(),
                        this.left + x,
                        this.top + TOP_BUFFER + y,
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
        graphics.drawString(this.font, this.title, this.left, this.top, 0xFF_FFFFFF, true); // title
    }

    @Override
    public void onClose() {
        consumer.accept(null);
        Minecraft.getInstance().setScreen(parent);
    }
}
