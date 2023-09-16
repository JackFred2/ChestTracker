package red.jackf.chesttracker.gui.screen;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.GuiConstants;
import red.jackf.chesttracker.gui.util.TextColours;
import red.jackf.chesttracker.gui.widget.*;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.memory.Metadata;
import red.jackf.chesttracker.storage.ConnectionSettings;
import red.jackf.chesttracker.storage.LoadContext;
import red.jackf.chesttracker.storage.Storage;

import java.util.*;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

/**
 * Shows a UI for managing an individual memory bank. Possibly the currently loaded bank.
 */
public class EditMemoryBankScreen extends BaseUtilScreen {
    private static final int CLOSE_BUTTON_SIZE = 12;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ID_TOP = 30;
    private static final int NAME_TOP = 45;
    private static final int SETTINGS_TOP = 60;
    private static final int SETTINGS_TAB_SELECTOR_WIDTH = 65;
    private static final int SETTINGS_MAX_COLUMNS = 2;
    @Nullable
    private EditBox nameEditBox = null;
    private final Screen parent;
    private final Runnable afterBankLoaded;
    private final MemoryBank memoryBank;
    @Nullable
    private StringSelectorWidget<SettingsTab> settingsTabSelector;
    private final Multimap<SettingsTab, AbstractWidget> settingsMap = LinkedListMultimap.create();

    protected EditMemoryBankScreen(@Nullable Screen parent, Runnable afterBankLoaded, String memoryBankId) {
        super(translatable("chesttracker.gui.editMemoryBank"));
        this.parent = parent;
        this.afterBankLoaded = afterBankLoaded;
        this.memoryBank = Storage.load(memoryBankId).orElse(null);
        if (this.memoryBank == null) {
            onClose();
        }
    }

    private boolean isCurrentIdLoaded() {
        return this.memoryBank == MemoryBank.INSTANCE;
    }

    @Override
    protected void init() {
        super.init();

        var font = Minecraft.getInstance().font;
        var inGame = Minecraft.getInstance().level != null;

        // close button
        this.addRenderableWidget(new ImageButton(
                left + menuWidth - (GuiConstants.SMALL_MARGIN + CLOSE_BUTTON_SIZE),
                top + GuiConstants.SMALL_MARGIN,
                CLOSE_BUTTON_SIZE,
                CLOSE_BUTTON_SIZE,
                0,
                0,
                CLOSE_BUTTON_SIZE,
                ChestTracker.guiTex("widgets/return_button"),
                CLOSE_BUTTON_SIZE,
                CLOSE_BUTTON_SIZE * 3,
                b -> this.onClose())).setTooltip(Tooltip.create(translatable("mco.selectServer.close")));

        // details label
        this.addRenderableOnly(new TextWidget(this.left + GuiConstants.MARGIN,
                top + GuiConstants.MARGIN,
                this.menuWidth - GuiConstants.MARGIN - 2 * GuiConstants.SMALL_MARGIN - CLOSE_BUTTON_SIZE,
                Storage.getBackendLabel(memoryBank.getId()),
                TextColours.getLabelColour(),
                TextWidget.Alignment.RIGHT));

        // ID
        var idLabel = translatable("chesttracker.gui.id");
        this.addRenderableOnly(new TextWidget(this.left + GuiConstants.MARGIN,
                this.top + ID_TOP,
                idLabel,
                TextColours.getLabelColour()));

        var bankIdText = Component.literal(this.memoryBank.getId());
        if (ChestTrackerConfig.INSTANCE.getConfig().gui.hideMemoryIds)
            bankIdText = bankIdText.withStyle(ChatFormatting.OBFUSCATED);
        this.addRenderableOnly(new TextWidget(this.left + GuiConstants.MARGIN + font.width(idLabel) + 4,
                this.top + ID_TOP,
                bankIdText,
                TextColours.getLabelColour()));

        // Name
        var nameLabel = translatable("mco.backup.entry.name");
        this.addRenderableOnly(new TextWidget(this.left + GuiConstants.MARGIN,
                this.top + NAME_TOP,
                nameLabel,
                TextColours.getLabelColour()));
        this.nameEditBox = this.addRenderableWidget(new CustomEditBox(font,
                this.left + GuiConstants.MARGIN + font.width(nameLabel) + GuiConstants.SMALL_MARGIN,
                this.top + NAME_TOP - 2,
                menuWidth - 2 * GuiConstants.MARGIN - font.width(nameLabel) - GuiConstants.SMALL_MARGIN,
                font.lineHeight + 3,
                this.nameEditBox,
                CommonComponents.EMPTY));
        this.nameEditBox.setResponder(s -> {
            if (s.isEmpty() && !ChestTrackerConfig.INSTANCE.getConfig().gui.hideMemoryIds) {
                this.nameEditBox.setHint(literal(CreateMemoryBankScreen.getNameFromId(this.memoryBank.getId())));
                this.nameEditBox.setTextColor(TextColours.getHintColour());
            } else {
                this.nameEditBox.setHint(CommonComponents.EMPTY);
                this.nameEditBox.setTextColor(TextColours.getTextColour());
            }
            this.memoryBank.getMetadata().setName(s.isEmpty() ? null : s);
        });
        this.nameEditBox.setValue(this.memoryBank.getMetadata().getName() != null ? this.memoryBank.getMetadata().getName() : "");

        // bottom button elements
        List<List<RenderableThingGetter<?>>> bottomButtons = new ArrayList<>();

        {
            List<RenderableThingGetter<?>> managementButtons = new ArrayList<>();
            // delete everything
            managementButtons.add((x, y, width, height) -> new HoldToConfirmButton(x, y, width, height,
                    translatable("selectServer.deleteButton"),
                    GuiConstants.ARE_YOU_REALLY_SURE_BUTTON_HOLD_TIME,
                    this::delete));

            managementButtons.add(((x, y, width, height) -> Button.builder(translatable("chesttracker.gui.editMemoryKeys"), this::openEditMemoryKeys)
                    .bounds(x, y, width, height)
                    .build()));

            bottomButtons.add(managementButtons);
        }

        List<RenderableThingGetter<?>> saveCreateLoadRow = new ArrayList<>();
        bottomButtons.add(saveCreateLoadRow);
        if (inGame && !isCurrentIdLoaded()) {
            // load
            saveCreateLoadRow.add((x, y, width, height) -> Button.builder(translatable("structure_block.mode.load"), this::load)
                    .bounds(x, y, width, height)
                    .build());
        }

        // save
        saveCreateLoadRow.add((x, y, width, height) -> Button.builder(translatable("selectWorld.edit.save"), this::save)
                .bounds(x, y, width, height)
                .build());

        // mark default if ingame
        if (inGame) {
            var ctx = LoadContext.get();
            var connectionSettings = ctx != null ? ConnectionSettings.get(ctx.connectionId()) : null;

            if (connectionSettings != null)
                saveCreateLoadRow.add((x, y, width, height) -> {
                    if (connectionSettings.memoryBankIdOverride().orElse(ctx.connectionId()).equals(this.memoryBank.getId())) {
                        // disable if already the default for the current connection
                        var defaultButton = Button.builder(translatable("chesttracker.gui.editMemoryBank.alreadyDefault"), b -> {
                                })
                                .bounds(x, y, width, height)
                                .build();
                        defaultButton.active = false;
                        return defaultButton;
                    } else {
                        return Button.builder(translatable("chesttracker.gui.editMemoryBank.markDefault"), this::markDefault)
                                .tooltip(Tooltip.create(translatable("chesttracker.gui.editMemoryBank.markDefault.tooltip")))
                                .bounds(x, y, width, height)
                                .build();
                    }
                });
        }

        addBottomButtons(bottomButtons);

        setupSettings(this.menuHeight - SETTINGS_TOP - 2 - (GuiConstants.MARGIN + BUTTON_HEIGHT) * bottomButtons.size());
    }

    private void openEditMemoryKeys(Button ignored) {
        Minecraft.getInstance().setScreen(new EditMemoryKeysScreen(this, memoryBank));
    }

    private void setupSettings(int height) {
        settingsTabSelector = this.addRenderableWidget(new StringSelectorWidget<>(this.left + GuiConstants.MARGIN,
                this.top + SETTINGS_TOP,
                SETTINGS_TAB_SELECTOR_WIDTH,
                height,
                CommonComponents.EMPTY,
                this::setSettingsTab));
        var selectorOptions = new LinkedHashMap<SettingsTab, Component>();
        selectorOptions.put(SettingsTab.FILTERING, Component.translatable("chesttracker.gui.editMemoryBank.filtering"));
        selectorOptions.put(SettingsTab.INTEGRITY, Component.translatable("chesttracker.gui.editMemoryBank.integrity"));
        selectorOptions.put(SettingsTab.EMPTY, Component.literal("Empty"));

        settingsTabSelector.setOptions(selectorOptions);

        setupFilteringSettings();
        setupIntegritySettings();

        setSettingsTab(SettingsTab.FILTERING);
    }

    private void setupFilteringSettings() {
        addSetting(CycleButton.onOffBuilder(this.memoryBank.getMetadata().getFilteringSettings().onlyRememberNamed)
                .withTooltip(b -> Tooltip.create(translatable("chesttracker.gui.editMemoryBank.filtering.onlyRemembedNamed.tooltip")))
                .create(getSettingsX(0),
                        getSettingsY(0),
                        getSettingsWidth(1),
                        BUTTON_HEIGHT,
                        translatable("chesttracker.gui.editMemoryBank.filtering.onlyRemembedNamed"),
                        (cycleButton, newValue) -> this.memoryBank.getMetadata().getFilteringSettings().onlyRememberNamed = newValue
                ), SettingsTab.FILTERING);
    }

    private void setupIntegritySettings() {
        addSetting(CycleButton.onOffBuilder(this.memoryBank.getMetadata().getIntegritySettings().preserveNamed)
                .withTooltip(b -> Tooltip.create(translatable("chesttracker.gui.editMemoryBank.integrity.preserveNamed.tooltip")))
                .create(getSettingsX(0),
                        getSettingsY(0),
                        getSettingsWidth(1),
                        BUTTON_HEIGHT,
                        translatable("chesttracker.gui.editMemoryBank.integrity.preserveNamed"),
                        (cycleButton, newValue) -> this.memoryBank.getMetadata().getIntegritySettings().preserveNamed = newValue
                ), SettingsTab.INTEGRITY);

        addSetting(CycleButton.<Metadata.IntegritySettings.LifetimeCountMode>builder(mode -> mode.label)
                .withValues(Metadata.IntegritySettings.LifetimeCountMode.values())
                .withInitialValue(this.memoryBank.getMetadata().getIntegritySettings().lifetimeCountMode)
                .displayOnlyValue()
                .create(
                        getSettingsX(1),
                        getSettingsY(0),
                        getSettingsWidth(1),
                        BUTTON_HEIGHT,
                        translatable("chesttracker.gui.editMemoryBank.integrity.lifetimeCountMode"),
                        ((cycleButton, countMode) -> this.memoryBank.getMetadata().getIntegritySettings().lifetimeCountMode = countMode)
                ), SettingsTab.INTEGRITY);

        addSetting(new EnumSlider<>(getSettingsX(0),
                getSettingsY(1),
                getSettingsWidth(2),
                BUTTON_HEIGHT,
                Metadata.IntegritySettings.MemoryLifetime.class,
                this.memoryBank.getMetadata().getIntegritySettings().memoryLifetime,
                lifetime -> lifetime.label,
                lifetime -> this.memoryBank.getMetadata().getIntegritySettings().memoryLifetime = lifetime), SettingsTab.INTEGRITY);

        addSetting(CycleButton.onOffBuilder(this.memoryBank.getMetadata().getIntegritySettings().removeOnPlayerBlockBreak)
                .withTooltip(b -> Tooltip.create(translatable("chesttracker.gui.editMemoryBank.integrity.blockBreak.tooltip")))
                .create(getSettingsX(0),
                        getSettingsY(2),
                        getSettingsWidth(1),
                        BUTTON_HEIGHT,
                        translatable("chesttracker.gui.editMemoryBank.integrity.blockBreak"),
                        (cycleButton, newValue) -> this.memoryBank.getMetadata().getIntegritySettings().removeOnPlayerBlockBreak = newValue
                ), SettingsTab.INTEGRITY);

        addSetting(CycleButton.onOffBuilder(this.memoryBank.getMetadata().getIntegritySettings().checkPeriodicallyForMissingBlocks)
                .withTooltip(b -> Tooltip.create(translatable("chesttracker.gui.editMemoryBank.integrity.periodicCheck.tooltip")))
                .create(getSettingsX(1),
                        getSettingsY(2),
                        getSettingsWidth(1),
                        BUTTON_HEIGHT,
                        translatable("chesttracker.gui.editMemoryBank.integrity.periodicCheck"),
                        (cycleButton, newValue) -> this.memoryBank.getMetadata().getIntegritySettings().checkPeriodicallyForMissingBlocks = newValue
                ), SettingsTab.INTEGRITY);
    }

    private void addSetting(AbstractWidget widget, SettingsTab tab) {
        this.addRenderableWidget(widget);
        widget.visible = false;
        settingsMap.put(tab, widget);
    }

    private void setSettingsTab(SettingsTab tab) {
        if (settingsTabSelector != null)
            settingsTabSelector.setHighlight(tab);
        for (Map.Entry<SettingsTab, AbstractWidget> entry : settingsMap.entries())
            entry.getValue().visible = entry.getKey() == tab;
    }

    private int getSingleSettingsColumnWidth() {
        final int settingsAreaWidth = this.menuWidth - GuiConstants.SMALL_MARGIN - SETTINGS_TAB_SELECTOR_WIDTH - 2 * GuiConstants.MARGIN;
        //noinspection PointlessArithmeticExpression
        return (settingsAreaWidth - GuiConstants.SMALL_MARGIN * (SETTINGS_MAX_COLUMNS - 1)) / SETTINGS_MAX_COLUMNS;
    }

    private int getSettingsX(int column) {
        final int columnWidth = getSingleSettingsColumnWidth();

        final int baseX = this.left + GuiConstants.MARGIN + SETTINGS_TAB_SELECTOR_WIDTH + GuiConstants.SMALL_MARGIN;
        return baseX + column * (columnWidth + GuiConstants.SMALL_MARGIN);
    }

    private int getSettingsY(int row) {
        final int baseY = this.top + SETTINGS_TOP;
        return baseY + row * (BUTTON_HEIGHT + GuiConstants.SMALL_MARGIN);
    }

    private int getSettingsWidth(int columnsTaken) {
        final int columnWidth = getSingleSettingsColumnWidth();

        return columnWidth * columnsTaken + GuiConstants.SMALL_MARGIN * (columnsTaken - 1);
    }

    //
    private void addBottomButtons(List<List<RenderableThingGetter<?>>> buttons) {
        final int rowWidth = this.menuWidth - 2 * GuiConstants.MARGIN;
        final int startX = this.left + GuiConstants.MARGIN;
        final int startY = this.top + this.menuHeight - GuiConstants.MARGIN - BUTTON_HEIGHT;
        final int yOffset = BUTTON_HEIGHT + GuiConstants.SMALL_MARGIN;

        // bottom upwards
        for (int i = 0; i < buttons.size(); i++) {
            var row = buttons.get(i);
            int buttonWidth = (rowWidth - (GuiConstants.SMALL_MARGIN * (row.size() - 1))) / row.size();
            for (int buttonIndex = 0; buttonIndex < row.size(); buttonIndex++) {
                this.addRenderableWidget(row.get(buttonIndex)
                        .get(startX + buttonIndex * (buttonWidth + GuiConstants.SMALL_MARGIN), startY - i * yOffset, buttonWidth, BUTTON_HEIGHT));
            }
        }
    }

    private void markDefault(Button button) {
        var ctx = LoadContext.get();
        if (ctx != null) {
            ConnectionSettings.put(ctx.connectionId(), ConnectionSettings.getOrCreate(ctx.connectionId())
                    .setOverride(this.memoryBank.getId().equals(ctx.connectionId()) ? Optional.empty() : Optional.of(this.memoryBank.getId())));
            button.active = false;
        }
    }

    // Load a memory bank, then run the load callback.
    private void load(Button button) {
        MemoryBank.loadOrCreate(this.memoryBank.getId(), this.memoryBank.getMetadata());
        afterBankLoaded.run();
    }

    // Delete the selected memory bank, and close the GUI.
    private void delete(HoldToConfirmButton button) {
        if (isCurrentIdLoaded()) MemoryBank.unload();
        Storage.delete(this.memoryBank.getId());
        this.onClose();
    }

    // Save the selected memory bank, and close the GUI.
    private void save(Button button) {
        var memory = Storage.load(this.memoryBank.getId()).orElseGet(() -> new MemoryBank(this.memoryBank.getMetadata(), new HashMap<>()));
        memory.setMetadata(this.memoryBank.getMetadata());
        Storage.save(memory);
        this.onClose();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @FunctionalInterface
    private interface RenderableThingGetter<T extends GuiEventListener & Renderable & NarratableEntry> {
        T get(int x, int y, int width, int height);
    }

    private enum SettingsTab {
        FILTERING,
        INTEGRITY,
        EMPTY
    }
}
