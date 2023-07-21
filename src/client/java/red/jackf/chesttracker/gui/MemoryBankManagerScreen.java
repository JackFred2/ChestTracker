package red.jackf.chesttracker.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.util.NinePatcher;
import red.jackf.chesttracker.gui.util.TextColours;
import red.jackf.chesttracker.gui.widget.CustomEditBox;
import red.jackf.chesttracker.gui.widget.StringSelectorWidget;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.storage.LoadContext;
import red.jackf.chesttracker.storage.StorageUtil;
import red.jackf.chesttracker.util.StringUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Allows a user to select a memory id, or create a new one.
 */
public class MemoryBankManagerScreen extends Screen {
    private static final int SCREEN_MARGIN = 50;
    private static final int MIN_WIDTH = 250;
    private static final int MAX_WIDTH = 400;
    private static final int MARGIN = 8;
    private static final int TITLE_TOP = 8;
    private static final int SEARCH_TOP = 19;
    private static final int SEARCH_HEIGHT = 12;
    private static final int LIST_TOP = 36;
    private static final int NEW_BUTTON_SIZE = 12;


    private final Screen parent;
    private final Runnable onSelect;
    private int menuWidth = 0;
    private int menuHeight = 0;
    private int left = 0;
    private int top = 0;

    private EditBox search = null;
    private StringSelectorWidget list;
    @Nullable
    private ImageButton newMemoryButton = null;
    private final List<String> ids;

    /**
     * @param parent - Screen to open on cancel, usually when pressing escape
     * @param onSelect - Runnable to run on selection (creation or selection of existing)
     */
    public MemoryBankManagerScreen(@Nullable Screen parent, Runnable onSelect) {
        super(Component.translatable("chesttracker.gui.memoryManager.title"));
        this.parent = parent;
        this.onSelect = onSelect;
        this.ids = StorageUtil.getStorage().getAllIds().stream().sorted().collect(Collectors.toList());
    }

    public MemoryBankManagerScreen(@Nullable Screen parent) {
        this(parent, () -> Minecraft.getInstance().setScreen(parent));
    }

    @Override
    protected void init() {
        super.init();

        this.menuWidth = Mth.clamp(this.width - 2 * SCREEN_MARGIN, MIN_WIDTH, MAX_WIDTH);
        this.menuHeight = this.height - 2 * SCREEN_MARGIN;

        this.menuWidth = NinePatcher.BACKGROUND.fitsNicely(this.menuWidth);
        this.menuHeight = NinePatcher.BACKGROUND.fitsNicely(this.menuHeight);
        this.left = (this.width - menuWidth) / 2;
        this.top = (this.height - menuHeight) / 2;

        var ingame = Minecraft.getInstance().level != null;

        if (ingame)
            newMemoryButton = this.addRenderableWidget(new ImageButton(this.left + menuWidth - NEW_BUTTON_SIZE - MARGIN,
                    this.top + SEARCH_TOP,
                    NEW_BUTTON_SIZE,
                    NEW_BUTTON_SIZE,
                    0,
                    0,
                    NEW_BUTTON_SIZE,
                    ChestTracker.guiTex("widgets/new_memory_bank_button"),
                    NEW_BUTTON_SIZE,
                    NEW_BUTTON_SIZE * 3,
                    b -> {
                        if (!this.search.getValue().isEmpty())
                            select(makeUserId(this.search.getValue()));
                    }));

        this.search = this.addRenderableWidget(new CustomEditBox(
                Minecraft.getInstance().font,
                this.left + MARGIN,
                this.top + SEARCH_TOP ,
                this.menuWidth - 2 * MARGIN - (ingame ? (NEW_BUTTON_SIZE + 6) : 0),
                SEARCH_HEIGHT,
                this.search,
                Component.translatable("chesttracker.gui.memoryManager.search")
        ));
        this.search.setTextColor(TextColours.getSearchTextColour());
        this.search.setBordered(false);
        this.search.setHint(Component.translatable("chesttracker.gui.memoryManager.search"));
        this.search.setResponder(term -> {
            this.list.setOptions(this.ids.stream()
                    .filter(id -> id.toLowerCase().contains(term.toLowerCase()))
                    .collect(Collectors.toList()));

            var resultTerm = makeUserId(term);

            if (newMemoryButton != null)
                if (term.isEmpty() || this.ids.contains(resultTerm)) {
                    newMemoryButton.active = false;
                    newMemoryButton.setTooltip(Tooltip.create(Component.translatable("chesttracker.gui.memoryManager.invalidName")));
                } else {
                    newMemoryButton.active = true;
                    newMemoryButton.setTooltip(Tooltip.create(Component.translatable("chesttracker.gui.memoryManager.newMemoryBank", resultTerm)));
                }
        });

        this.list = this.addRenderableWidget(new StringSelectorWidget(
                this.left + MARGIN,
                this.top + LIST_TOP,
                this.menuWidth - 2 * MARGIN,
                this.menuHeight - LIST_TOP - MARGIN,
                CommonComponents.EMPTY,
                this::select
        ));

        this.search.setValue("");
    }

    private String makeUserId(String id) {
        return "user/" + StringUtil.sanitizeForPath(id);
    }

    private void select(String memoryId) {
        var context = LoadContext.get(Minecraft.getInstance());
        if (context == null) return;
        MemoryBank.loadOrCreate(memoryId, context);
        this.onSelect.run();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        NinePatcher.BACKGROUND.draw(graphics, this.left, this.top, this.menuWidth, this.menuHeight);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(Minecraft.getInstance().font, this.title, left + MARGIN, this.top + TITLE_TOP, TextColours.getTitleColour(), false);
        var backendText = Component.translatable("chesttracker.gui.memoryManager.selectedBackend", ChestTrackerConfig.INSTANCE.getConfig().memory.storageBackend.name());
        var textWidth = Minecraft.getInstance().font.width(backendText);
        graphics.drawString(Minecraft.getInstance().font, backendText, left + menuWidth - MARGIN - textWidth, this.top + TITLE_TOP, TextColours.getTitleColour(), false);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
