package red.jackf.chesttracker.gui.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntComparators;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static red.jackf.chesttracker.ChestTracker.id;

@Environment(EnvType.CLIENT)
public class WItemListPanel extends WGridPanel {
    private static final Identifier SLOT = id("textures/slot.png");
    private static final Identifier SLOT_RED = id("textures/slot_red.png");
    private static final Style TOOLTIP_STYLE = Style.EMPTY.withItalic(false).withColor(Formatting.GREEN);
    private List<ItemStack> items = Collections.emptyList();
    private List<ItemStack> filteredItems = Collections.emptyList();
    @Nullable
    private BiConsumer<Integer, Integer> pageChangeHook = null;

    private final int columns;
    private final int rows;

    private String filter = "";
    private int currentPage = 1;
    private int pageCount = 1;
    private boolean usable = true;

    public WItemListPanel(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
    }

    public void setUsable(boolean usable) {
        this.usable = usable;
    }

    public void setItems(List<ItemStack> items) {
        this.items = items.stream()//.map(ItemStack::copy)
            .sorted(Collections.reverseOrder(Comparator.comparingInt(ItemStack::getCount)))
            .collect(Collectors.toList());
        this.updateFilter();
    }

    private static Text getCountText(ItemStack stack) {
        return new TranslatableText("chesttracker.gui.stack_count", stack.getCount()).setStyle(TOOLTIP_STYLE);
    }

    private void updateFilter() {
        this.filteredItems = items.stream().filter(stack -> stack.getName().getString().toLowerCase().contains(this.filter)
            || stack.getTag() != null && stack.getTag().toString().toLowerCase().contains(this.filter)).collect(Collectors.toList());
        this.pageCount = ((filteredItems.size() - 1) / (columns * rows)) + 1;
        this.currentPage = Math.min(currentPage, pageCount);
        if (this.pageChangeHook != null) this.pageChangeHook.accept(this.currentPage, this.pageCount);
    }

    private static String getLabel(int count) {
        if (count < 1_000) return "" + count;
        if (count < 1_000_000) return count / 1_000 + "k";
        if (count < 1_000_000_000) return count / 1_000_000 + "M";
        return count / 1_000_000_000 + "G";
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        super.paint(matrices, x, y, mouseX, mouseY);

        RenderSystem.enableDepthTest();
        MinecraftClient mc = MinecraftClient.getInstance();
        ItemRenderer renderer = mc.getItemRenderer();

        int cellsPerPage = columns * rows;
        int startIndex = cellsPerPage * (currentPage - 1);

        for (int i = startIndex; i < Math.min(startIndex + cellsPerPage, filteredItems.size()); i++) {
            ItemStack stack = filteredItems.get(i);
            int renderX = x + 18 * ((i % cellsPerPage) % columns);
            int renderY = y + (18 * ((i % cellsPerPage) / columns));

            mc.getTextureManager().bindTexture(SLOT);
            //mc.getTextureManager().bindTexture(usable ? SLOT : SLOT_RED);
            DrawableHelper.drawTexture(matrices, renderX, renderY, 10, 0, 0, 18, 18, 18, 18);

            renderer.zOffset = 100f;
            renderer.renderInGui(stack, renderX + 1, renderY + 1);
            renderer.renderGuiItemOverlay(mc.textRenderer, stack, renderX + 1, renderY + 1, getLabel(stack.getCount()));
            renderer.zOffset = 0f;

            int mouseXAbs = (int) (mc.mouse.getX() / mc.getWindow().getScaleFactor());
            int mouseYAbs = (int) (mc.mouse.getY() / mc.getWindow().getScaleFactor());

            if ((renderX <= mouseXAbs && mouseXAbs < renderX + 18) && (renderY <= mouseYAbs && mouseYAbs < renderY + 18)) {
                matrices.translate(0, 0, 400);
                DrawableHelper.fill(matrices, renderX + 1, renderY + 1, renderX + 17, renderY + 17, 0x5affffff);
                matrices.translate(0, 0, -400);
            }
        }
    }

    public void nextPage() {
        setPage(this.currentPage + 1);
    }

    public void previousPage() {
        setPage(this.currentPage - 1);
    }

    @Override
    public void onMouseScroll(int x, int y, double amount) {
        setPage(this.currentPage - (int) amount);
    }

    private void setPage(int newPage) {
        this.currentPage = MathHelper.clamp(newPage, 1, this.pageCount);
        if (this.pageChangeHook != null) this.pageChangeHook.accept(this.currentPage, this.pageCount);
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F + (0.4f * (float) (this.currentPage - 1) / this.pageCount)));
    }

    private void searchItem(int x, int y) {
        int cellsPerPage = columns * rows;
        int startIndex = cellsPerPage * (currentPage - 1);

        int relX = x / 18;
        int relY = y / 18;

        int itemIndex = startIndex + relX + (relY * columns);
        if (itemIndex < filteredItems.size()) {
            if (MinecraftClient.getInstance().player != null) {
                ClientPlayerEntity playerEntity = MinecraftClient.getInstance().player;
                ItemStack stack = this.filteredItems.get(itemIndex);
                ChestTracker.searchForItem(stack, playerEntity.world);
            }
        }
    }

    @Override
    public void onClick(int x, int y, int button) {
        if (usable && button == 0) {
            searchItem(x, y);
        }
    }

    @Override
    public void renderTooltip(MatrixStack matrices, int x, int y, int tX, int tY) {
        int cellsPerPage = columns * rows;
        int startIndex = cellsPerPage * (currentPage - 1);

        int relX = (tX - this.x) / 18;
        int relY = (tY - this.y) / 18;

        int itemIndex = startIndex + relX + (relY * columns);
        if (itemIndex < filteredItems.size()) {
            ItemStack stack = this.filteredItems.get(itemIndex);
            List<Text> tooltips = stack.getTooltip(null, TooltipContext.Default.NORMAL);
            tooltips.add(getCountText(stack));

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

    public void setPageChangeHook(@Nullable BiConsumer<Integer, Integer> pageChangeHook) {
        this.pageChangeHook = pageChangeHook;
    }
}
