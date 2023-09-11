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
import red.jackf.chesttracker.storage.StorageUtil;
import red.jackf.chesttracker.util.StringUtil;

import java.util.*;

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
    private final String memoryBankId;
    private final Metadata metadata;
    private final boolean isCreatingNewBank;
    @Nullable
    private StringSelectorWidget<SettingsTab> settingsTabSelector;
    private final Multimap<SettingsTab, AbstractWidget> settingsMap = LinkedListMultimap.create();

    protected EditMemoryBankScreen(@Nullable Screen parent, Runnable afterBankLoaded, @Nullable String memoryBankId) {
        super(translatable("chesttracker.gui.editMemoryBank." + (memoryBankId == null ? "create" : "edit")));
        this.parent = parent;
        this.afterBankLoaded = afterBankLoaded;
        this.isCreatingNewBank = memoryBankId == null;
        if (isCreatingNewBank) {
            this.memoryBankId = getNextIdDefault();
            this.metadata = Metadata.blankWithName(this.memoryBankId.substring("user/".length()));
        } else {
            this.memoryBankId = memoryBankId;
            var metadata = StorageUtil.getStorage().getMetadata(memoryBankId);
            this.metadata = metadata == null ? Metadata.blank() : metadata;
        }
    }

    private String makeUserId(String id) {
        return "user/" + StringUtil.sanitizeForPath(id);
    }

    private boolean isCurrentIdLoaded() {
        return MemoryBank.INSTANCE != null && MemoryBank.INSTANCE.getId().equals(memoryBankId);
    }

    private String getNextIdDefault() {
        var keys = StorageUtil.getStorage().getAllIds();
        int index = 1;
        String id;
        do {
            id = makeUserId("custom" + index++);
        } while (keys.contains(id));
        return id;
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
        if (!isCreatingNewBank)
            this.addRenderableOnly(new TextWidget(this.left + GuiConstants.MARGIN,
                    top + GuiConstants.MARGIN,
                    this.menuWidth - GuiConstants.MARGIN - 2 * GuiConstants.SMALL_MARGIN - CLOSE_BUTTON_SIZE,
                    StorageUtil.getStorage().getDescriptionLabel(memoryBankId),
                    TextColours.getLabelColour(),
                    TextWidget.Alignment.RIGHT));

        // ID
        var idLabel = translatable("chesttracker.gui.editMemoryBank.id");
        this.addRenderableOnly(new TextWidget(this.left + GuiConstants.MARGIN,
                this.top + ID_TOP,
                idLabel,
                TextColours.getLabelColour()));

        var bankIdText = Component.literal(memoryBankId);
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
                this.left + GuiConstants.MARGIN + font.width(nameLabel) + 4,
                this.top + NAME_TOP - 2,
                menuWidth - 2 * GuiConstants.MARGIN - font.width(nameLabel) - 4,
                font.lineHeight + 3,
                this.nameEditBox,
                CommonComponents.EMPTY));
        this.nameEditBox.setResponder(s -> {
            if (s.isEmpty() && !ChestTrackerConfig.INSTANCE.getConfig().gui.hideMemoryIds) {
                this.nameEditBox.setHint(Component.literal(memoryBankId));
                this.nameEditBox.setTextColor(TextColours.getSearchHintColour());
            } else {
                this.nameEditBox.setHint(CommonComponents.EMPTY);
                this.nameEditBox.setTextColor(TextColours.getSearchTextColour());
            }
            this.metadata.setName(s.isEmpty() ? null : s);
        });
        this.nameEditBox.setValue(metadata.getName() != null ? metadata.getName() : "");

        // bottom button elements
        List<List<RenderableThingGetter<?>>> bottomButtons = new ArrayList<>();

        if (StorageUtil.getStorage().exists(memoryBankId)) {
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
            // [create and] load
            saveCreateLoadRow.add((x, y, width, height) -> Button.builder(translatable(isCreatingNewBank ? "chesttracker.gui.editMemoryBank.createAndLoad" : "structure_block.mode.load"), this::loadOrCreate)
                    .bounds(x, y, width, height)
                    .build());
        }

        if (!isCreatingNewBank) {
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
                        if (connectionSettings.memoryBankIdOverride().orElse(ctx.connectionId()).equals(memoryBankId)) {
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
        } else {
            // add a button to create a bank for the default ID, if not existing
            var ctx = LoadContext.get();
            if (ctx != null && !StorageUtil.getStorage().exists(ctx.connectionId()))
                bottomButtons.add(List.of((x, y, width, height) -> Button.builder(translatable("chesttracker.gui.editMemoryBank.createDefault"), ignored -> createDefault(ctx))
                        .bounds(x, y, width, height)
                        .build()));
        }

        addBottomButtons(bottomButtons);

        setupSettings(this.menuHeight - SETTINGS_TOP - 2 - (GuiConstants.MARGIN + BUTTON_HEIGHT) * bottomButtons.size());
    }

    private void openEditMemoryKeys(Button ignored) {
        Minecraft.getInstance().setScreen(new EditMemoryKeysScreen(this, memoryBankId));
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
        addSetting(CycleButton.onOffBuilder(metadata.getFilteringSettings().onlyRememberNamed)
                .withTooltip(b -> Tooltip.create(translatable("chesttracker.gui.editMemoryBank.filtering.onlyRemembedNamed.tooltip")))
                .create(getSettingsX(0),
                        getSettingsY(0),
                        getSettingsWidth(1),
                        BUTTON_HEIGHT,
                        translatable("chesttracker.gui.editMemoryBank.filtering.onlyRemembedNamed"),
                        (cycleButton, newValue) -> metadata.getFilteringSettings().onlyRememberNamed = newValue
                ), SettingsTab.FILTERING);
    }

    private void setupIntegritySettings() {
        addSetting(CycleButton.onOffBuilder(metadata.getIntegritySettings().removeOnPlayerBlockBreak)
                .withTooltip(b -> Tooltip.create(translatable("chesttracker.gui.editMemoryBank.integrity.blockBreak.tooltip")))
                .create(getSettingsX(0),
                        getSettingsY(1),
                        getSettingsWidth(1),
                        BUTTON_HEIGHT,
                        translatable("chesttracker.gui.editMemoryBank.integrity.blockBreak"),
                        (cycleButton, newValue) -> metadata.getIntegritySettings().removeOnPlayerBlockBreak = newValue
                ), SettingsTab.INTEGRITY);

        addSetting(CycleButton.onOffBuilder(metadata.getIntegritySettings().checkPeriodicallyForMissingBlocks)
                .withTooltip(b -> Tooltip.create(translatable("chesttracker.gui.editMemoryBank.integrity.periodicCheck.tooltip")))
                .create(getSettingsX(1),
                        getSettingsY(1),
                        getSettingsWidth(1),
                        BUTTON_HEIGHT,
                        translatable("chesttracker.gui.editMemoryBank.integrity.periodicCheck"),
                        (cycleButton, newValue) -> metadata.getIntegritySettings().checkPeriodicallyForMissingBlocks = newValue
                ), SettingsTab.INTEGRITY);

        addSetting(CycleButton.onOffBuilder(metadata.getIntegritySettings().preserveNamed)
                .withTooltip(b -> Tooltip.create(translatable("chesttracker.gui.editMemoryBank.integrity.preserveNamed.tooltip")))
                .create(getSettingsX(0),
                        getSettingsY(2),
                        getSettingsWidth(1),
                        BUTTON_HEIGHT,
                        translatable("chesttracker.gui.editMemoryBank.integrity.preserveNamed"),
                        (cycleButton, newValue) -> metadata.getIntegritySettings().preserveNamed = newValue
                ), SettingsTab.INTEGRITY);

        addSetting(new EnumSlider<>(getSettingsX(0),
                getSettingsY(0),
                getSettingsWidth(2),
                BUTTON_HEIGHT,
                Metadata.IntegritySettings.MemoryLifetime.class,
                metadata.getIntegritySettings().memoryLifetime,
                lifetime -> lifetime.label,
                lifetime -> metadata.getIntegritySettings().memoryLifetime = lifetime), SettingsTab.INTEGRITY);
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
                    .setOverride(memoryBankId.equals(ctx.connectionId()) ? Optional.empty() : Optional.of(memoryBankId)));
            button.active = false;
        }
    }

    // Create and load a memory bank using the current load context, then run the load callback.
    private void loadOrCreate(Button button) {
        MemoryBank.loadOrCreate(memoryBankId, metadata);
        afterBankLoaded.run();
    }

    // Create and load a memory bank using the current load context, then run the load callback.
    private void createDefault(LoadContext ctx) {
        MemoryBank.loadOrCreate(ctx.connectionId(), metadata);
        afterBankLoaded.run();
    }

    // Delete the selected memory bank, and close the GUI.
    private void delete(HoldToConfirmButton button) {
        if (isCurrentIdLoaded()) MemoryBank.unload();
        StorageUtil.getStorage().delete(memoryBankId);
        this.onClose();
    }

    // Save the selected memory bank, and close the GUI.
    private void save(Button button) {
        if (isCurrentIdLoaded()) {
            //noinspection DataFlowIssue
            MemoryBank.INSTANCE.setMetadata(metadata);
            MemoryBank.save();
        } else {
            var memory = StorageUtil.getStorage().load(memoryBankId);
            if (memory != null) {
                memory.setMetadata(metadata);
            } else {
                memory = new MemoryBank(metadata, new HashMap<>());
            }
            memory.setId(memoryBankId);
            StorageUtil.getStorage().save(memory);
        }
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
