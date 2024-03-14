package red.jackf.chesttracker.gui.screen;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.GuiConstants;
import red.jackf.chesttracker.gui.util.TextColours;
import red.jackf.chesttracker.gui.widget.*;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.memory.metadata.CompatibilitySettings;
import red.jackf.chesttracker.memory.metadata.FilteringSettings;
import red.jackf.chesttracker.memory.metadata.IntegritySettings;
import red.jackf.chesttracker.memory.metadata.SearchSettings;
import red.jackf.chesttracker.provider.ProviderHandler;
import red.jackf.chesttracker.storage.ConnectionSettings;
import red.jackf.chesttracker.storage.Storage;
import red.jackf.chesttracker.util.GuiUtil;
import red.jackf.chesttracker.util.I18nUtil;
import red.jackf.chesttracker.util.Misc;
import red.jackf.jackfredlib.client.api.gps.Coordinate;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.client.WhereIsItClient;
import red.jackf.whereisit.client.render.Rendering;

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
    private final boolean isCurrentLoaded;
    @Nullable
    private EditBox nameEditBox = null;
    private final Screen parent;
    private final Runnable afterBankLoaded;
    private final MemoryBankView memoryBank;
    private final Multimap<SettingsTab, AbstractWidget> settingsMap = LinkedListMultimap.create();
    @Nullable
    private StringSelectorWidget<SettingsTab> settingsTabSelector;
    @Nullable
    private Button pruneWithinRange;
    @Nullable
    private Button pruneOutsideRange;

    private static int manageWorkingRange = 256;

    protected EditMemoryBankScreen(@Nullable Screen parent, Runnable afterBankLoaded, String memoryBankId) {
        super(translatable("chesttracker.gui.editMemoryBank"));
        this.parent = parent;
        this.afterBankLoaded = afterBankLoaded;
        var memoryBank = Storage.load(memoryBankId).orElse(null);
        if (memoryBank == null) {
            onClose();

            // wont compile if not used
            this.isCurrentLoaded = false;
            this.memoryBank = MemoryBankView.empty();
        } else {
            this.isCurrentLoaded = memoryBank == MemoryBank.INSTANCE;
            this.memoryBank = MemoryBankView.of(memoryBank);
        }
    }

    @Override
    protected void init() {
        super.init();

        var font = Minecraft.getInstance().font;
        var inGame = Minecraft.getInstance().level != null;

        // close button
        this.addRenderableWidget(GuiUtil.close(
                left + menuWidth - (GuiConstants.SMALL_MARGIN + CLOSE_BUTTON_SIZE),
                top + GuiConstants.SMALL_MARGIN,
                b -> this.onClose()));

        // details label
        this.addRenderableOnly(new TextWidget(this.left + GuiConstants.MARGIN,
                                              top + GuiConstants.MARGIN,
                                              this.menuWidth - GuiConstants.MARGIN - 2 * GuiConstants.SMALL_MARGIN - CLOSE_BUTTON_SIZE,
                                              Storage.getBackendLabel(memoryBank.id()),
                                              TextColours.getLabelColour(),
                                              TextWidget.Alignment.RIGHT));

        // ID
        var idLabel = translatable("chesttracker.gui.id");
        this.addRenderableOnly(new TextWidget(this.left + GuiConstants.MARGIN,
                                              this.top + ID_TOP,
                                              idLabel,
                                              TextColours.getLabelColour()));

        var bankIdText = Component.literal(this.memoryBank.id());
        if (ChestTrackerConfig.INSTANCE.instance().gui.hideMemoryIds)
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
            if (s.isEmpty() && !ChestTrackerConfig.INSTANCE.instance().gui.hideMemoryIds) {
                this.nameEditBox.setHint(literal(CreateMemoryBankScreen.getNameFromId(this.memoryBank.id())));
                this.nameEditBox.setTextColor(TextColours.getHintColour());
            } else {
                this.nameEditBox.setHint(CommonComponents.EMPTY);
                this.nameEditBox.setTextColor(TextColours.getTextColour());
            }
            this.memoryBank.metadata().setName(s.isEmpty() ? null : s);
        });
        this.nameEditBox.setValue(Optional.ofNullable(this.memoryBank.metadata().getName()).orElse(""));

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
        if (inGame && !isCurrentLoaded) {
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
            Optional<Coordinate> coord = Coordinate.getCurrent();
            coord.map(coordinate -> ConnectionSettings.get(coordinate.id()))
                 .ifPresent(connectionSettings -> saveCreateLoadRow.add((x, y, width, height) -> {
                     if (connectionSettings.memoryBankIdOverride().orElse(coord.get().id())
                                           .equals(this.memoryBank.id())) {
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
                 }));

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
        selectorOptions.put(SettingsTab.COMPATIBILITY, translatable("chesttracker.gui.editMemoryBank.compatibility"));
        selectorOptions.put(SettingsTab.FILTERING, translatable("chesttracker.gui.editMemoryBank.filtering"));
        selectorOptions.put(SettingsTab.INTEGRITY, translatable("chesttracker.gui.editMemoryBank.integrity"));
        if (isCurrentLoaded)
            selectorOptions.put(SettingsTab.MANAGE, translatable("chesttracker.gui.editMemoryBank.manage"));
        selectorOptions.put(SettingsTab.SEARCH, translatable("chesttracker.gui.editMemoryBank.search"));
        selectorOptions.put(SettingsTab.EMPTY, CommonComponents.EMPTY);

        settingsTabSelector.setOptions(selectorOptions);

        setupCompatibilitySettings();
        setupFilteringSettings();
        setupIntegritySettings();
        if (isCurrentLoaded) setupManagementSettings();
        setupSearchSettings();

        addSetting(new StringWidget(getSettingsX(0),
                                    getSettingsY(0),
                                    getSettingsWidth(1),
                                    BUTTON_HEIGHT,
                                    literal("^_^").withStyle(ChatFormatting.BOLD),
                                    font).setColor(0x4040FF), SettingsTab.EMPTY);

        setSettingsTab(SettingsTab.FILTERING);
    }

    ///////////////////
    // COMPATIBILITY //
    ///////////////////

    private void setupCompatibilitySettings() {
        addSetting(CycleButton.<CompatibilitySettings.NameFilterMode>builder(mode -> mode.label)
                .withTooltip(mode -> Tooltip.create(translatable("chesttracker.gui.editMemoryBank.compatibility.nameFilterMode.tooltip")
                        .append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE)
                        .append(mode.tooltip)))
                .withValues(CompatibilitySettings.NameFilterMode.values())
                .withInitialValue(this.memoryBank.metadata().getCompatibilitySettings().nameFilterMode)
                .create(getSettingsX(0),
                        getSettingsY(0),
                        getSettingsWidth(2),
                        BUTTON_HEIGHT,
                        translatable("chesttracker.gui.editMemoryBank.compatibility.nameFilterMode"),
                        (cycleButton, newValue) -> this.memoryBank.metadata().getCompatibilitySettings().nameFilterMode = newValue
                ), SettingsTab.COMPATIBILITY);
    }

    ///////////////
    // FILTERING //
    ///////////////

    private void setupFilteringSettings() {
        addSetting(CycleButton.onOffBuilder(this.memoryBank.metadata().getFilteringSettings().onlyRememberNamed)
                              .withTooltip(b -> Tooltip.create(translatable("chesttracker.gui.editMemoryBank.filtering.onlyRemembedNamed.tooltip")))
                              .create(getSettingsX(0),
                                      getSettingsY(0),
                                      getSettingsWidth(1),
                                      BUTTON_HEIGHT,
                                      translatable("chesttracker.gui.editMemoryBank.filtering.onlyRemembedNamed"),
                                      (cycleButton, newValue) -> this.memoryBank.metadata()
                                                                                .getFilteringSettings().onlyRememberNamed = newValue
                              ), SettingsTab.FILTERING);

        addSetting(CycleButton.<FilteringSettings.RememberedContainers>builder(remembered -> remembered.label)
                              .displayOnlyValue()
                              .withTooltip(remembered -> Tooltip.create(remembered.tooltip))
                              .withValues(FilteringSettings.RememberedContainers.values())
                              .withInitialValue(this.memoryBank.metadata().getFilteringSettings().rememberedContainers)
                              .create(getSettingsX(1),
                                      getSettingsY(0),
                                      getSettingsWidth(1),
                                      BUTTON_HEIGHT,
                                      CommonComponents.EMPTY,
                                      (cycleButton, remembered) -> this.memoryBank.metadata()
                                                                                  .getFilteringSettings().rememberedContainers = remembered
                              ), SettingsTab.FILTERING);

        addSetting(CycleButton.<FilteringSettings.AutoAddPlacedBlocks>builder(remembered -> remembered.label)
                              .withValues(FilteringSettings.AutoAddPlacedBlocks.values())
                              .withInitialValue(this.memoryBank.metadata().getFilteringSettings().autoAddPlacedBlocks)
                              .create(getSettingsX(0),
                                      getSettingsY(1),
                                      getSettingsWidth(2),
                                      BUTTON_HEIGHT,
                                      translatable("chesttracker.gui.editMemoryBank.filtering.autoAddPlacedBlocks"),
                                      (cycleButton, autoAdd) -> this.memoryBank.metadata()
                                                                               .getFilteringSettings().autoAddPlacedBlocks = autoAdd
                              ), SettingsTab.FILTERING);

        addSetting(CycleButton.onOffBuilder(this.memoryBank.metadata().getFilteringSettings().rememberEnderChests)
                              .withTooltip(b -> Tooltip.create(translatable("chesttracker.gui.editMemoryBank.filtering.rememberEnderChests.tooltip")))
                              .create(getSettingsX(0),
                                      getSettingsY(2),
                                      getSettingsWidth(1),
                                      BUTTON_HEIGHT,
                                      translatable("chesttracker.gui.editMemoryBank.filtering.rememberEnderChests"),
                                      (cycleButton, newValue) -> this.memoryBank.metadata()
                                                                                .getFilteringSettings().rememberEnderChests = newValue
                              ), SettingsTab.FILTERING);
    }

    ///////////////
    // INTEGRITY //
    ///////////////

    private void setupIntegritySettings() {
        addSetting(CycleButton.onOffBuilder(this.memoryBank.metadata().getIntegritySettings().preserveNamed)
                              .withTooltip(b -> Tooltip.create(translatable("chesttracker.gui.editMemoryBank.integrity.preserveNamed.tooltip")))
                              .create(getSettingsX(0),
                                      getSettingsY(0),
                                      getSettingsWidth(1),
                                      BUTTON_HEIGHT,
                                      translatable("chesttracker.gui.editMemoryBank.integrity.preserveNamed"),
                                      (cycleButton, newValue) -> this.memoryBank.metadata()
                                                                                .getIntegritySettings().preserveNamed = newValue
                              ), SettingsTab.INTEGRITY);

        addSetting(CycleButton.<IntegritySettings.LifetimeCountMode>builder(mode -> mode.label)
                              .withValues(IntegritySettings.LifetimeCountMode.values())
                              .withInitialValue(this.memoryBank.metadata().getIntegritySettings().lifetimeCountMode)
                              .displayOnlyValue()
                              .create(getSettingsX(1),
                                      getSettingsY(0),
                                      getSettingsWidth(1),
                                      BUTTON_HEIGHT,
                                      translatable("chesttracker.gui.editMemoryBank.integrity.lifetimeCountMode"),
                                      ((cycleButton, countMode) -> this.memoryBank.metadata()
                                                                                  .getIntegritySettings().lifetimeCountMode = countMode)
                              ), SettingsTab.INTEGRITY);

        addSetting(new EnumSlider<>(getSettingsX(0),
                                    getSettingsY(1),
                                    getSettingsWidth(2),
                                    BUTTON_HEIGHT,
                                    IntegritySettings.MemoryLifetime.class,
                                    this.memoryBank.metadata().getIntegritySettings().memoryLifetime,
                                    lifetime -> lifetime.label) {
            @Override
            protected void applyValue() {
                EditMemoryBankScreen.this.memoryBank.metadata().getIntegritySettings().memoryLifetime = getSelected();
            }
        }, SettingsTab.INTEGRITY);

        addSetting(CycleButton.onOffBuilder(this.memoryBank.metadata()
                                                           .getIntegritySettings().removeOnPlayerBlockBreak)
                              .withTooltip(b -> Tooltip.create(translatable("chesttracker.gui.editMemoryBank.integrity.blockBreak.tooltip")))
                              .create(getSettingsX(0),
                                      getSettingsY(2),
                                      getSettingsWidth(1),
                                      BUTTON_HEIGHT,
                                      translatable("chesttracker.gui.editMemoryBank.integrity.blockBreak"),
                                      (cycleButton, newValue) -> this.memoryBank.metadata()
                                                                                .getIntegritySettings().removeOnPlayerBlockBreak = newValue
                              ), SettingsTab.INTEGRITY);

        addSetting(CycleButton.onOffBuilder(this.memoryBank.metadata()
                                                           .getIntegritySettings().checkPeriodicallyForMissingBlocks)
                              .withTooltip(b -> Tooltip.create(translatable("chesttracker.gui.editMemoryBank.integrity.periodicCheck.tooltip")))
                              .create(getSettingsX(1),
                                      getSettingsY(2),
                                      getSettingsWidth(1),
                                      BUTTON_HEIGHT,
                                      translatable("chesttracker.gui.editMemoryBank.integrity.periodicCheck"),
                                      (cycleButton, newValue) -> this.memoryBank.metadata()
                                                                                .getIntegritySettings().checkPeriodicallyForMissingBlocks = newValue
                              ), SettingsTab.INTEGRITY);
    }

    ////////////////
    // MANAGEMENT //
    ////////////////

    private void setupManagementSettings() {
        pruneWithinRange = Button.builder(CommonComponents.EMPTY, b -> pruneWithinRange())
                                 .bounds(getSettingsX(0), getSettingsY(1), getSettingsWidth(2), BUTTON_HEIGHT)
                                 .build();
        pruneOutsideRange = Button.builder(CommonComponents.EMPTY, b -> pruneOutsideRange())
                                  .bounds(getSettingsX(0), getSettingsY(2), getSettingsWidth(2), BUTTON_HEIGHT)
                                  .build();

        addSetting(pruneWithinRange, SettingsTab.MANAGE);
        addSetting(pruneOutsideRange, SettingsTab.MANAGE);
        addSetting(new SteppedSlider<>(getSettingsX(0),
                                       getSettingsY(0),
                                       getSettingsWidth(2),
                                       BUTTON_HEIGHT,
                                       SearchSettings.SEARCH_RANGES_NO_INFINITE,
                                       manageWorkingRange,
                                       range -> translatable("chesttracker.gui.editMemoryBank.manage.workingRange",
                                                             I18nUtil.blocks(range == Integer.MAX_VALUE ? translatable("effect.duration.infinite") : range))) {

            @Override
            protected void applyValue() {
                manageWorkingRange = getSelected();

                refreshManagementButtons();
            }
        }, SettingsTab.MANAGE);

        addSetting(Button.builder(translatable("chesttracker.gui.editMemoryBank.manage.highlightAll"), b -> highlightAll())
                         .bounds(getSettingsX(0),
                                 getSettingsY(3),
                                 getSettingsWidth(1),
                                 BUTTON_HEIGHT)
                         .build(), SettingsTab.MANAGE);

        refreshManagementButtons();
    }

    private void highlightAll() {
        var currentKey = ProviderHandler.getCurrentKey();
        if (currentKey == null) return;
        var currentMemories = memoryBank.getMemories(currentKey);
        if (currentMemories == null) return;

        // TODO make this not an internal hack
        WhereIsItClient.closedScreenThisSearch = false;
        Rendering.resetSearchTime();
        WhereIsItClient.recieveResults(currentMemories.entrySet().stream()
                                                      .map(e -> SearchResult.builder(e.getKey())
                                                                            .name(e.getValue().name(), null)
                                                                            .otherPositions(e.getValue().otherPositions())
                                                                            .build())
                                                      .toList());
    }

    private void refreshManagementButtons() {
        Pair<Set<BlockPos>, Set<BlockPos>> counts = partitionMemoriesInCurrentKey();

        if (pruneWithinRange != null) {
            pruneWithinRange.setMessage(translatable("chesttracker.gui.editMemoryBank.manage.deleteWithinRange", manageWorkingRange, counts.getFirst()
                                                                                                                                           .size()));
        }
        if (pruneOutsideRange != null) {
            pruneOutsideRange.setMessage(translatable("chesttracker.gui.editMemoryBank.manage.deleteOutsideRange", manageWorkingRange, counts.getSecond()
                                                                                                                                             .size()));
        }
    }

    private Pair<Set<BlockPos>, Set<BlockPos>> partitionMemoriesInCurrentKey() {
        final Pair<Set<BlockPos>, Set<BlockPos>> empty = Pair.of(Collections.emptySet(), Collections.emptySet());
        if (Minecraft.getInstance().player == null) return empty;
        var currentKey = ProviderHandler.getCurrentKey();
        if (currentKey == null) return empty;
        var currentMemories = memoryBank.getMemories(currentKey);
        if (currentMemories == null) return empty;

        final double squareRange = (double) manageWorkingRange * manageWorkingRange;
        final Vec3 origin = Minecraft.getInstance().player.getEyePosition();
        Set<BlockPos> within = new HashSet<>();
        Set<BlockPos> outside = new HashSet<>();
        for (BlockPos pos : currentMemories.keySet()) {
            if (origin.distanceToSqr(pos.getCenter()) < squareRange) {
                within.add(pos);
            } else {
                outside.add(pos);
            }
        }

        return Pair.of(within, outside);
    }

    private void pruneOutsideRange() {
        var currentKey = ProviderHandler.getCurrentKey();
        if (currentKey == null) return;

        Pair<Set<BlockPos>, Set<BlockPos>> counts = partitionMemoriesInCurrentKey();

        for (BlockPos pos : counts.getSecond()) {
            memoryBank.remove(currentKey, pos);
        }

        refreshManagementButtons();
    }

    private void pruneWithinRange() {
        var currentKey = ProviderHandler.getCurrentKey();
        if (currentKey == null) return;

        Pair<Set<BlockPos>, Set<BlockPos>> counts = partitionMemoriesInCurrentKey();

        for (BlockPos pos : counts.getFirst()) {
            memoryBank.remove(currentKey, pos);
        }

        refreshManagementButtons();
    }

    ////////////
    // SEARCH //
    ////////////

    private void setupSearchSettings() {
        addSetting(Misc.let(new SteppedSlider<>(getSettingsX(0),
                                                getSettingsY(0),
                                                getSettingsWidth(2),
                                                BUTTON_HEIGHT,
                                                SearchSettings.SEARCH_RANGES,
                                                this.memoryBank.metadata().getSearchSettings().itemListRange,
                                                range -> translatable("chesttracker.gui.editMemoryBank.search.itemListRange",
                                                                      I18nUtil.blocks(range == Integer.MAX_VALUE ? translatable("effect.duration.infinite") : range))) {

            @Override
            protected void applyValue() {
                EditMemoryBankScreen.this.memoryBank.metadata().getSearchSettings().itemListRange = getSelected();
            }
        }, slider -> slider.setTooltip(Tooltip.create(translatable("chesttracker.gui.editMemoryBank.search.itemListRange.tooltip")))), SettingsTab.SEARCH);

        addSetting(new SteppedSlider<>(getSettingsX(0),
                                       getSettingsY(1),
                                       getSettingsWidth(2),
                                       BUTTON_HEIGHT,
                                       SearchSettings.SEARCH_RANGES,
                                       this.memoryBank.metadata().getSearchSettings().searchRange,
                                       range -> translatable("chesttracker.gui.editMemoryBank.search.searchRange",
                                                             I18nUtil.blocks(range == Integer.MAX_VALUE ? translatable("effect.duration.infinite") : range))) {
            @Override
            protected void applyValue() {
                EditMemoryBankScreen.this.memoryBank.metadata().getSearchSettings().searchRange = getSelected();
            }
        }, SettingsTab.SEARCH);

        addSetting(CycleButton.<MemoryBank.StackMergeMode>builder(mode -> mode.label)
                .withValues(MemoryBank.StackMergeMode.values())
                .withTooltip(ignored -> Tooltip.create(translatable("chesttracker.gui.editMemoryBank.search.stackMergeMode.tooltip")))
                .withInitialValue(this.memoryBank.metadata().getSearchSettings().stackMergeMode)
                .create(getSettingsX(0),
                        getSettingsY(2),
                        getSettingsWidth(2),
                        BUTTON_HEIGHT,
                        translatable("chesttracker.gui.editMemoryBank.search.stackMergeMode"),
                        ((cycleButton, stackMergeMode) -> this.memoryBank.metadata()
                                .getSearchSettings().stackMergeMode = stackMergeMode)
                ), SettingsTab.SEARCH);
    }

    ///////////
    // UTILS //
    ///////////

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
        var ctx = Coordinate.getCurrent();
        if (ctx.isPresent()) {
            ConnectionSettings.put(ctx.get().id(), ConnectionSettings.getOrCreate(ctx.get().id())
                                                                     .setOverride(this.memoryBank.id()
                                                                                                 .equals(ctx.get()
                                                                                                            .id()) ? Optional.empty() : Optional.of(this.memoryBank.id())));
            button.active = false;
        }
    }

    // Load a memory bank, then run the load callback.
    private void load(Button button) {
        MemoryBank.loadOrCreate(this.memoryBank.id(), this.memoryBank.metadata());
        afterBankLoaded.run();
    }

    // Delete the selected memory bank, and close the GUI.
    private void delete(HoldToConfirmButton button) {
        if (isCurrentLoaded) MemoryBank.unload();
        Storage.delete(this.memoryBank.id());
        this.onClose();
    }

    // Save the selected memory bank, and close the GUI.
    private void save(Button button) {
        this.memoryBank.save();
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
        COMPATIBILITY,
        FILTERING,
        INTEGRITY,
        MANAGE,
        SEARCH,
        EMPTY
    }
}
