package red.jackf.chesttracker.gui.screen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.GuiConstants;
import red.jackf.chesttracker.gui.util.TextColours;
import red.jackf.chesttracker.gui.widget.CustomEditBox;
import red.jackf.chesttracker.gui.widget.StringSelectorWidget;
import red.jackf.chesttracker.gui.widget.TextWidget;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.memory.Metadata;
import red.jackf.chesttracker.storage.StorageUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Allows a user to select (if in game) and manage memory banks.
 */
public class MemoryBankManagerScreen extends BaseUtilScreen {
    private static final int BUTTON_SIZE = 12;
    private static final int SEARCH_TOP = 19;
    private static final int SEARCH_HEIGHT = 12;
    private static final int LIST_TOP = 36;


    private final Runnable onRemoveScreen;
    private final Runnable afterBankLoaded;

    private EditBox search = null;
    private StringSelectorWidget<String> memoryBankList;
    private Map<String, Metadata> memoryBanks;

    /**
     * @param onRemoveScreen  - Runnable to run on cancel, usually when pressing escape or a back button
     * @param afterBankLoaded - Runnable to run after selection of a new memory bank
     */
    public MemoryBankManagerScreen(Runnable onRemoveScreen, Runnable afterBankLoaded) {
        super(Component.translatable("chesttracker.gui.memoryManager"));
        this.onRemoveScreen = onRemoveScreen;
        this.afterBankLoaded = afterBankLoaded;
    }

    /**
     * @param onRemoveScreen  - Screen to open on cancel, usually when pressing escape or a back button
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

        // backend label
        this.addRenderableOnly(new TextWidget(this.left + GuiConstants.MARGIN,
                top + GuiConstants.MARGIN,
                this.menuWidth - GuiConstants.MARGIN - 2 * GuiConstants.SMALL_MARGIN - BUTTON_SIZE,
                Component.translatable("chesttracker.gui.memoryManager.selectedBackend", ChestTrackerConfig.INSTANCE.getConfig().storage.storageBackend.name()),
                TextColours.getLabelColour(),
                TextWidget.Alignment.RIGHT));

        // close button
        this.addRenderableWidget(new ImageButton(
                this.left + this.menuWidth - BUTTON_SIZE - GuiConstants.SMALL_MARGIN,
                this.top + GuiConstants.SMALL_MARGIN,
                BUTTON_SIZE,
                BUTTON_SIZE,
                0,
                0,
                BUTTON_SIZE,
                ChestTracker.guiTex("widgets/return_button"),
                BUTTON_SIZE,
                BUTTON_SIZE * 3,
                b -> this.onClose())).setTooltip(Tooltip.create(Component.translatable("mco.selectServer.close")));

        var inGame = Minecraft.getInstance().level != null;

        if (inGame) {
            // button to create a new memory; not shown if not ingame
            this.addRenderableWidget(new ImageButton(
                            this.left + menuWidth - BUTTON_SIZE - GuiConstants.SMALL_MARGIN,
                            this.top + SEARCH_TOP,
                            BUTTON_SIZE,
                            BUTTON_SIZE,
                            0,
                            0,
                            BUTTON_SIZE,
                            ChestTracker.guiTex("widgets/new_memory_bank_button"),
                            BUTTON_SIZE,
                            BUTTON_SIZE * 3,
                            b -> openEditScreen(afterBankLoaded, null)))
                    .setTooltip(Tooltip.create(Component.translatable("chesttracker.gui.memoryManager.newMemoryBank")));
        }

        // search bar
        this.search = this.addRenderableWidget(new CustomEditBox(
                Minecraft.getInstance().font,
                this.left + GuiConstants.MARGIN,
                this.top + SEARCH_TOP,
                this.menuWidth - 2 * GuiConstants.SMALL_MARGIN - (inGame ? (BUTTON_SIZE + 6) : 0),
                SEARCH_HEIGHT,
                this.search,
                Component.translatable("chesttracker.gui.memoryManager.search")
        ));
        this.search.setTextColor(TextColours.getSearchTextColour());
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
                            e -> {
                                if (e.getValue().getName() != null) {
                                    return Component.literal(e.getValue().getName()); // custom user-defined name
                                } else {
                                    var id = Component.literal(e.getKey());
                                    if (ChestTrackerConfig.INSTANCE.getConfig().gui.hideMemoryIds)
                                        id.setStyle(Style.EMPTY.withObfuscated(true));
                                    return id;
                                }
                            },
                            (a, b) -> a,
                            LinkedHashMap::new
                    )));
        });

        // String list
        this.memoryBankList = this.addRenderableWidget(new StringSelectorWidget<>(
                this.left + GuiConstants.MARGIN,
                this.top + LIST_TOP,
                this.menuWidth - 2 * GuiConstants.MARGIN,
                this.menuHeight - LIST_TOP - GuiConstants.MARGIN,
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
    public void onClose() {
        onRemoveScreen.run();
    }
}
