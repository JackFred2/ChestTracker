package red.jackf.chesttracker.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
        private static final Identifier LEFT_BUTTON = id("textures/left_button.png");
        private static final Identifier RIGHT_BUTTON = id("textures/right_button.png");

        private static final int COLUMNS = 9;
        private static final int ROWS = 6;
        private static final int SIDE_PADDING = 0;
        private static final int TOP_PADDING = 36;

        private final WItemListPanel itemPanel;
        private final WUpdatableTextField searchField;

        public Gui() {
            int width = (18 * COLUMNS) + (2 * SIDE_PADDING);
            int height = (18 * ROWS) + SIDE_PADDING + TOP_PADDING;

            WPlainPanel root = new WPlainPanel();
            root.setSize(width, height);
            setRootPanel(root);

            itemPanel = new WItemListPanel();
            root.add(itemPanel, SIDE_PADDING, TOP_PADDING, 18 * COLUMNS, 18 * ROWS);

            List<ItemRepresentation> stacks = new ArrayList<>();
            for (int i = 0; i < Registry.ITEM.stream().count() - 1; i++) {
                ItemRepresentation representation = new ItemRepresentation(new ItemStack(Registry.ITEM.get(i + 1)), id("default"));
                if (new Random().nextFloat() < 0.1f) representation.setVisible(false);
                stacks.add(representation);
            }

            searchField = new WUpdatableTextField(new TranslatableText("chesttracker.gui.search_filed_start"));
            searchField.setOnTextChanged(itemPanel::setFilter);
            root.add(searchField, SIDE_PADDING, TOP_PADDING - 23, 18 * (COLUMNS - 3), 20);

            WButton leftButton = new WButton(new TextureIcon(LEFT_BUTTON));
            WButton rightButton = new WButton(new TextureIcon(RIGHT_BUTTON));

            //root.add(leftButton, width - 72, TOP_PADDING - 30, 24, 24);
            //root.add(rightButton, width - 36, TOP_PADDING - 30, 24, 24);

            setItems(stacks);

            root.validate(this);
        }

        public boolean isTyping() {
            return this.searchField.isFocused();
        }

        private void setItems(List<ItemRepresentation> items) {
            this.itemPanel.setItems(items);
        }
    }

    public static class WItemListPanel extends WGridPanel {
        private static final Identifier SLOT = id("textures/slot.png");
        private static final Identifier SLOT_RED = id("textures/slot_red.png");
        private List<ItemRepresentation> items = Collections.emptyList();
        private List<ItemRepresentation> filteredItems = Collections.emptyList();

        private String filter = "";
        private int currentPage = 1;
        private int pageCount = 1;

        public void clear() {
            this.items.clear();
        }

        public void setItems(@NotNull List<ItemRepresentation> items) {
            this.items = items;
            this.updateFilter();
        }

        private void updateFilter() {
            this.filteredItems = items.stream().filter(representation -> representation.getStack().getName().getString().toLowerCase().contains(this.filter)).collect(Collectors.toList());
            this.pageCount = ((filteredItems.size() - 1) / (Gui.COLUMNS * Gui.ROWS)) + 1;
            this.currentPage = Math.min(currentPage, pageCount);
        }

        @Override
        public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
            super.paint(matrices, x, y, mouseX, mouseY);

            RenderSystem.enableDepthTest();
            MinecraftClient mc = MinecraftClient.getInstance();
            ItemRenderer renderer = mc.getItemRenderer();

            int cellsPerPage = Gui.COLUMNS * Gui.ROWS;
            int startIndex = cellsPerPage * (currentPage - 1);

            for (int i = startIndex; i < Math.min(startIndex + cellsPerPage, filteredItems.size()); i++) {
                ItemRepresentation representation = filteredItems.get(i);
                int renderX = x + 18 * ((i % cellsPerPage) % Gui.COLUMNS);
                int renderY = y + (18 * ((i % cellsPerPage) / Gui.COLUMNS));

                mc.getTextureManager().bindTexture(representation.isVisible() ? SLOT : SLOT_RED);
                DrawableHelper.drawTexture(matrices, renderX, renderY, 10, 0, 0, 18, 18, 18, 18);

                renderer.zOffset = 100f;
                renderer.renderInGui(representation.stack, renderX + 1, renderY + 1);
                renderer.zOffset = 0f;

                int mouseXAbs = (int) (mc.mouse.getX() / mc.getWindow().getScaleFactor());
                int mouseYAbs = (int) (mc.mouse.getY() / mc.getWindow().getScaleFactor());

                if ((renderX <= mouseXAbs && mouseXAbs < renderX + 18) && (renderY <= mouseYAbs && mouseYAbs < renderY + 18)) {
                    matrices.translate(0, 0, 400);
                    DrawableHelper.fill(matrices, renderX + 1, renderY + 1, renderX + 16, renderY + 16, 0x5affffff);
                    matrices.translate(0, 0, -400);
                }
            }
        }

        @Override
        public void onMouseScroll(int x, int y, double amount) {
            this.currentPage = MathHelper.clamp(this.currentPage - (int) amount, 1, this.pageCount);
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, (float) (1.0F + (amount / 15f))));
        }

        @Override
        public void onClick(int x, int y, int button) {
            int cellsPerPage = Gui.COLUMNS * Gui.ROWS;
            int startIndex = cellsPerPage * (currentPage - 1);

            int relX = x / 18;
            int relY = y / 18;

            int itemIndex = startIndex + relX + (relY * Gui.COLUMNS);
            if (itemIndex < filteredItems.size()) {
                ItemRepresentation representation = this.filteredItems.get(itemIndex);
                System.out.println(representation);
            }
        }

        @Override
        public void renderTooltip(MatrixStack matrices, int x, int y, int tX, int tY) {
            int cellsPerPage = Gui.COLUMNS * Gui.ROWS;
            int startIndex = cellsPerPage * (currentPage - 1);

            int relX = (tX - this.x) / 18;
            int relY = (tY - this.y) / 18;

            int itemIndex = startIndex + relX + (relY * Gui.COLUMNS);
            if (itemIndex < filteredItems.size()) {
                List<Text> tooltips = this.filteredItems.get(itemIndex).getStack().getTooltip(null, TooltipContext.Default.NORMAL);

                Screen screen = MinecraftClient.getInstance().currentScreen;
                if (screen != null)
                    screen.renderOrderedTooltip(matrices, Lists.transform(tooltips, Text::asOrderedText), tX + x, tY + y);
            } else {
                super.renderTooltip(matrices, x, y, tX, tY);
            }
        }

        public void setFilter(String filter) {
            this.filter = filter;
            this.updateFilter();
        }
    }

    public static class WUpdatableTextField extends WTextField {
        private Consumer<String> onTextChanged;

        public WUpdatableTextField(Text text) {
            super(text);
        }

        @Override
        public void onCharTyped(char ch) {
            super.onCharTyped(ch);
            if (onTextChanged != null) onTextChanged.accept(this.text);
        }

        @Override
        public void onKeyPressed(int ch, int key, int modifiers) {
            super.onKeyPressed(ch, key, modifiers);
            if (onTextChanged != null) onTextChanged.accept(this.text);
        }

        public void setOnTextChanged(Consumer<String> onCharTyped) {
            this.onTextChanged = onCharTyped;
        }
    }

    /**
     * Extended item representation for
     */
    public static class ItemRepresentation {
        private final ItemStack stack;
        private final Identifier memoryId;
        private boolean isVisible = true;

        private ItemRepresentation(@NotNull ItemStack stack, @NotNull Identifier dimensionId) {
            this.stack = stack;
            this.memoryId = dimensionId;
        }

        private void setVisible(boolean visible) {
            isVisible = visible;
        }

        private boolean isVisible() {
            return isVisible;
        }

        private Identifier getMemoryId() {
            return memoryId;
        }

        private ItemStack getStack() {
            return stack;
        }

        @Override
        public String toString() {
            return "ItemRepresentation{" +
                "stack=" + stack +
                ", memoryId=" + memoryId +
                ", isVisible=" + isVisible +
                '}';
        }
    }
}
