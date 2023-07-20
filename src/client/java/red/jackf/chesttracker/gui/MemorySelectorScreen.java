package red.jackf.chesttracker.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.util.CustomSearchablesFormatter;
import red.jackf.chesttracker.gui.util.NinePatcher;
import red.jackf.chesttracker.gui.widget.CustomEditBox;
import red.jackf.chesttracker.gui.widget.StringSelectorWidget;
import red.jackf.chesttracker.storage.StorageUtil;

import java.util.List;
import java.util.stream.Collectors;

public class MemorySelectorScreen extends Screen {
    private static final int SCREEN_MARGIN = 50;
    private static final int MIN_WIDTH = 200;
    private static final int MAX_WIDTH = 400;
    private static final int MARGIN = 8;
    private static final int TITLE_TOP = 8;
    private static final int SEARCH_TOP = 19;
    private static final int SEARCH_HEIGHT = 12;
    private static final int LIST_TOP = 36;

    private final Screen parent;
    private int menuWidth = 0;
    private int menuHeight = 0;
    private int left = 0;
    private int top = 0;

    private EditBox search = null;
    private StringSelectorWidget list;
    private final List<String> ids;

    public MemorySelectorScreen(@Nullable Screen parent) {
        super(Component.translatable("chesttracker.gui.selectMemories"));
        this.parent = parent;
        this.ids = StorageUtil.getStorage().getAllIds().stream().sorted().collect(Collectors.toList());
    }

    @Override
    protected void init() {
        super.init();

        this.menuWidth = Mth.clamp(this.width - 2 * SCREEN_MARGIN, MIN_WIDTH, MAX_WIDTH);
        this.menuHeight = this.height - 2 * SCREEN_MARGIN;
        this.left = (this.width - menuWidth) / 2;
        this.top = (this.height - menuHeight) / 2;

        this.search = this.addRenderableWidget(new CustomEditBox(
                Minecraft.getInstance().font,
                this.left + MARGIN,
                this.top + SEARCH_TOP ,
                this.menuWidth - 2 * MARGIN,
                SEARCH_HEIGHT,
                this.search,
                Component.translatable("chesttracker.gui.searchMemories")
        ));
        this.search.setTextColor(CustomSearchablesFormatter.getTextColour());
        this.search.setBordered(false);
        this.search.setHint(Component.translatable("chesttracker.gui.searchMemories"));
        this.search.setResponder(term -> this.list.setOptions(this.ids.stream()
                .filter(id -> id.toLowerCase().contains(term.toLowerCase()))
                .collect(Collectors.toList())));

        this.list = this.addRenderableWidget(new StringSelectorWidget(
                this.left + MARGIN,
                this.top + LIST_TOP,
                this.menuWidth - 2 * MARGIN,
                this.menuHeight - LIST_TOP - MARGIN,
                CommonComponents.EMPTY
        ));

        this.list.setOptions(ids);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        NinePatcher.BACKGROUND.draw(graphics, this.left, this.top, this.menuWidth, this.menuHeight);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(Minecraft.getInstance().font, this.title, left + MARGIN, this.top + TITLE_TOP, ChestTrackerScreen.titleColour, false);
        var backendText = Component.translatable("chesttracker.gui.selectedBackend", ChestTrackerConfig.INSTANCE.getConfig().memory.storageBackend.name());
        var textWidth = Minecraft.getInstance().font.width(backendText);
        graphics.drawString(Minecraft.getInstance().font, backendText, left + menuWidth - MARGIN - textWidth, this.top + TITLE_TOP, ChestTrackerScreen.titleColour, false);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
