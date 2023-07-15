package red.jackf.chesttracker.config.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.gui.widget.ItemButton;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ItemSelectorScreen extends Screen {
    private static final int TOP_BUFFER = 40;
    private static final int HORIZONTAL_PADDING = 20;
    private static final int TOP_PADDING = 10;
    private static final int BOTTOM_PADDING = 20;
    private final Screen parent;
    private final Consumer<@Nullable Item> consumer;
    private final List<Item> items;
    private List<Item> filteredItems;
    private int menuWidth;
    private int menuHeight;
    private EditBox search;
    private int left = 0;
    private int top = 0;

    protected ItemSelectorScreen(Screen parent, Consumer<@Nullable Item> consumer) {
        super(Component.translatable("chesttracker.config.gui.memoryIcons.iconScreen"));
        this.parent = parent;
        this.consumer = consumer;

        this.items = BuiltInRegistries.ITEM.stream().filter(item -> item != Items.AIR).toList();
        this.filteredItems = new ArrayList<>(this.items);
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
            this.filteredItems = this.items.stream()
                    .filter(item -> item.getDescription().getString().toLowerCase().contains(s))
                    .collect(Collectors.toList());
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

        int index = 0;

        for (int y = 0; y < menuHeight - TOP_BUFFER; y += spacing) {
            for (int x = 0; x < menuWidth; x += spacing) {
                if (this.filteredItems.size() == index) return;
                var item = this.filteredItems.get(index++);
                this.addRenderableWidget(new ItemButton(new ItemStack(item),
                        this.left + x,
                        this.top + TOP_BUFFER + y,
                        item.getDescription(), b -> {
                    ItemSelectorScreen.this.consumer.accept(item);
                    this.onClose();
                }));
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
        Minecraft.getInstance().setScreen(parent);
    }
}
