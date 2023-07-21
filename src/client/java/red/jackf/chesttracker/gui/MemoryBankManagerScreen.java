package red.jackf.chesttracker.gui;

import com.mojang.datafixers.util.Pair;
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
import red.jackf.chesttracker.storage.StorageUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Allows a user to select (if in game) and manage memory banks.
 */
public class MemoryBankManagerScreen extends Screen {
    private static final int SCREEN_MARGIN = 50;
    private static final int MIN_WIDTH = 250;
    private static final int MAX_WIDTH = 400;
    private static final int MARGIN = 8;
    private static final int BUTTON_MARGIN = 5;
    private static final int CLOSE_BUTTON_RIGHT = 20;
    private static final int CLOSE_BUTTON_SIZE = 12;
    private static final int SEARCH_TOP = 19;
    private static final int SEARCH_HEIGHT = 12;
    private static final int LIST_TOP = 36;
    private static final int NEW_BUTTON_SIZE = 12;


    private final Runnable onRemoveScreen;
    private final Runnable afterBankLoaded;
    private int menuWidth = 0;
    private int menuHeight = 0;
    private int left = 0;
    private int top = 0;

    private EditBox search = null;
    private StringSelectorWidget<String> memoryBankList;
    private Map<String, MemoryBank.Metadata> memoryBanks;

    /**
     * @param onRemoveScreen - Runnable to run on cancel, usually when pressing escape or a back button
     * @param afterBankLoaded - Runnable to run after selection of a new memory bank
     */
    public MemoryBankManagerScreen(Runnable onRemoveScreen, Runnable afterBankLoaded) {
        super(Component.translatable("chesttracker.gui.memoryManager.title"));
        this.onRemoveScreen = onRemoveScreen;
        this.afterBankLoaded = afterBankLoaded;
    }

    /**
     * @param onRemoveScreen - Screen to open on cancel, usually when pressing escape or a back button
     * @param afterBankLoaded - Runnable to run after selection of a new memory bank
     */
    public MemoryBankManagerScreen(Supplier<@Nullable Screen> onRemoveScreen, Runnable afterBankLoaded) {
        this(() -> Minecraft.getInstance().setScreen(onRemoveScreen.get()), afterBankLoaded);
    }

    public MemoryBankManagerScreen(@Nullable Screen parent, Runnable afterBankLoaded) {
        this(() -> parent, afterBankLoaded);
    }

    @Override
    protected void init() {
        super.init();

        this.memoryBanks = StorageUtil.getStorage().getAllIds().stream()
                .sorted()
                .map(id -> Pair.of(id, StorageUtil.getStorage().getMetadata(id)))
                .filter(pair -> pair.getSecond() != null)
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (a, b) -> a, LinkedHashMap::new));

        this.menuWidth = Mth.clamp(this.width - 2 * SCREEN_MARGIN, MIN_WIDTH, MAX_WIDTH);
        this.menuHeight = this.height - 2 * SCREEN_MARGIN;

        this.menuWidth = NinePatcher.BACKGROUND.fitsNicely(this.menuWidth);
        this.menuHeight = NinePatcher.BACKGROUND.fitsNicely(this.menuHeight);
        this.left = (this.width - menuWidth) / 2;
        this.top = (this.height - menuHeight) / 2;

        // close button
        this.addRenderableWidget(new ImageButton(
                left + menuWidth - CLOSE_BUTTON_RIGHT,
                top + BUTTON_MARGIN,
                CLOSE_BUTTON_SIZE,
                CLOSE_BUTTON_SIZE,
                0,
                0,
                CLOSE_BUTTON_SIZE,
                ChestTracker.guiTex("widgets/return_button"),
                CLOSE_BUTTON_SIZE,
                CLOSE_BUTTON_SIZE * 3,
                b -> this.onClose())).setTooltip(Tooltip.create(Component.translatable("mco.selectServer.close")));

        var inGame = Minecraft.getInstance().level != null;

        if (inGame) {
            // button to create a new memory; not shown if not ingame
            this.addRenderableWidget(new ImageButton(this.left + menuWidth - NEW_BUTTON_SIZE - MARGIN,
                    this.top + SEARCH_TOP,
                    NEW_BUTTON_SIZE,
                    NEW_BUTTON_SIZE,
                    0,
                    0,
                    NEW_BUTTON_SIZE,
                    ChestTracker.guiTex("widgets/new_memory_bank_button"),
                    NEW_BUTTON_SIZE,
                    NEW_BUTTON_SIZE * 3,
                    b -> openEditScreen(afterBankLoaded, null)))
                    .setTooltip(Tooltip.create(Component.translatable("chesttracker.gui.memoryManager.newMemoryBank")));
        }

        // search bar
        this.search = this.addRenderableWidget(new CustomEditBox(
                Minecraft.getInstance().font,
                this.left + MARGIN,
                this.top + SEARCH_TOP ,
                this.menuWidth - 2 * MARGIN - (inGame ? (NEW_BUTTON_SIZE + 6) : 0),
                SEARCH_HEIGHT,
                this.search,
                Component.translatable("chesttracker.gui.memoryManager.search")
        ));
        this.search.setTextColor(TextColours.getSearchTextColour());
        this.search.setBordered(false);
        this.search.setHint(Component.translatable("chesttracker.gui.memoryManager.search"));
        this.search.setResponder(term -> {
            // update string list options
            this.memoryBankList.setOptions(this.memoryBanks.entrySet().stream()
                    .filter(entry -> {
                        var name = entry.getValue().getName();
                        if (name == null) name = entry.getKey();
                        return name.toLowerCase().contains(term.toLowerCase());
                    }).collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().getName() != null ? e.getValue().getName() : e.getKey(),
                            (a, b) -> a,
                            LinkedHashMap::new
                    )));
        });

        // String list
        this.memoryBankList = this.addRenderableWidget(new StringSelectorWidget<>(
                this.left + MARGIN,
                this.top + LIST_TOP,
                this.menuWidth - 2 * MARGIN,
                this.menuHeight - LIST_TOP - MARGIN,
                CommonComponents.EMPTY,
                id -> openEditScreen(afterBankLoaded, id)
        ));
        this.memoryBankList.setHighlight(MemoryBank.INSTANCE != null ? MemoryBank.INSTANCE.getId() : null);

        this.search.setValue("");
    }

    private void openEditScreen(Runnable afterBankLoaded, @Nullable String idToOpen) {
        Minecraft.getInstance().setScreen(new EditMemoryBankScreen(this, afterBankLoaded, idToOpen));
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        NinePatcher.BACKGROUND.draw(graphics, this.left, this.top, this.menuWidth, this.menuHeight);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(Minecraft.getInstance().font, this.title, left + MARGIN, this.top + MARGIN, TextColours.getLabelColour(), false);
        var backendText = Component.translatable("chesttracker.gui.memoryManager.selectedBackend", ChestTrackerConfig.INSTANCE.getConfig().memory.storageBackend.name());
        var textWidth = Minecraft.getInstance().font.width(backendText);
        graphics.drawString(Minecraft.getInstance().font, backendText, left + menuWidth - MARGIN - textWidth - BUTTON_MARGIN - CLOSE_BUTTON_SIZE, this.top + MARGIN, TextColours.getLabelColour(), false);
    }

    @Override
    public void onClose() {
        onRemoveScreen.run();
    }
}
