package red.jackf.chesttracker.gui.screen;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.util.NinePatcher;
import red.jackf.chesttracker.gui.util.TextColours;
import red.jackf.chesttracker.gui.widget.CustomEditBox;
import red.jackf.chesttracker.gui.widget.HoldToConfirmButton;
import red.jackf.chesttracker.gui.widget.StringSelectorWidget;
import red.jackf.chesttracker.gui.widget.TextWidget;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.memory.Metadata;
import red.jackf.chesttracker.storage.ConnectionSettings;
import red.jackf.chesttracker.storage.LoadContext;
import red.jackf.chesttracker.storage.StorageUtil;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.StringUtil;

import java.util.*;

import static net.minecraft.network.chat.Component.translatable;

/**
 * Shows a UI for managing an individual memory bank. Possibly the currently loaded bank.
 */
public class EditMemoryBankScreen extends Screen {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 220;
    private static final int MARGIN = 8;
    private static final int BUTTON_MARGIN = 5;
    private static final int CLOSE_BUTTON_SIZE = 12;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ID_TOP = 30;
    private static final int NAME_TOP = 45;
    private static final int SETTINGS_TOP = 60;
    private static final int SETTINGS_TAB_SELECTOR_WIDTH = 65;
    private static final int SETTINGS_MAX_COLUMNS = 2;
    private int menuWidth = 0;
    private int menuHeight = 0;
    private int left = 0;
    private int top = 0;
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
            this.metadata = Metadata.from(this.memoryBankId.substring("user/".length()));
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

        this.menuWidth = WIDTH;
        this.menuHeight = HEIGHT;
        this.left = (this.width - menuWidth) / 2;
        this.top = (this.height - menuHeight) / 2;

        // close button
        this.addRenderableWidget(new ImageButton(
                left + menuWidth - (BUTTON_MARGIN + CLOSE_BUTTON_SIZE),
                top + BUTTON_MARGIN,
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
        if (!isCreatingNewBank) {
            this.addRenderableOnly(new TextWidget(this.left + MARGIN,
                    top + MARGIN,
                    this.menuWidth - MARGIN - 2 * BUTTON_MARGIN - CLOSE_BUTTON_SIZE,
                    StorageUtil.getStorage().getDescriptionLabel(memoryBankId),
                    TextColours.getLabelColour(),
                    TextWidget.Alignment.RIGHT));
        }

        // ID
        var idLabel = translatable("chesttracker.gui.editMemoryBank.id");
        this.addRenderableOnly(new TextWidget(this.left + MARGIN,
                this.top + ID_TOP,
                idLabel,
                TextColours.getLabelColour()));

        var bankIdText = Component.literal(memoryBankId);
        if (ChestTrackerConfig.INSTANCE.getConfig().gui.hideMemoryIds)
            bankIdText = bankIdText.withStyle(ChatFormatting.OBFUSCATED);
        this.addRenderableOnly(new TextWidget(this.left + MARGIN + font.width(idLabel) + 4,
                this.top + ID_TOP,
                bankIdText,
                TextColours.getLabelColour()));

        // Name
        var nameLabel = translatable("mco.backup.entry.name");
        this.addRenderableOnly(new TextWidget(this.left + MARGIN,
                this.top + NAME_TOP,
                nameLabel,
                TextColours.getLabelColour()));
        this.nameEditBox = this.addRenderableWidget(new CustomEditBox(font,
                this.left + MARGIN + font.width(nameLabel) + 4,
                this.top + NAME_TOP - 2,
                menuWidth - 2 * MARGIN - font.width(nameLabel) - 4,
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

        // bottom buttonelement
        List<List<RenderableThingGetter<?>>> bottomButtons = new ArrayList<>();

        // delete everything
        if (StorageUtil.getStorage().getAllIds().contains(memoryBankId)) {
            List<RenderableThingGetter<?>> deleteButtons = new ArrayList<>(2);

            deleteButtons.add((x, y, width, height) -> new HoldToConfirmButton(x, y, width, height,
                    translatable("selectServer.deleteButton"),
                    Constants.ARE_YOU_REALLY_SURE_BUTTON_HOLD_TIME,
                    this::delete));

            deleteButtons.add(((x, y, width, height) -> Button.builder(translatable("chesttracker.gui.editMemoryBank.manageKeys"), this::openDeleteKeys)
                    .bounds(x, y, width, height)
                    .build()));

            bottomButtons.add(deleteButtons);
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
            if (ctx != null && !StorageUtil.getStorage().getAllIds().contains(ctx.connectionId()))
                bottomButtons.add(List.of((x, y, width, height) -> Button.builder(translatable("chesttracker.gui.editMemoryBank.createDefault"), ignored -> createDefault(ctx))
                        .bounds(x, y, width, height)
                        .build()));
        }

        // metadata settings
        /*List<RenderableThingGetter<?>> integrity = new ArrayList<>();
        integrity.add((x, y, width, height) -> {
            return CycleButton.onOffBuilder(metadata.getIntegritySettings().removeOnPlayerBlockBreak)
                    .create(x, y, width, height, translatable("chesttracker.gui.editMemoryBank.integrity.blockBreak"));
        });
        integrity.add((x, y, width, height) -> {
            return CycleButton.builder(Metadata.IntegritySettings.NameHandling::getLabel)
                    .withValues(Metadata.IntegritySettings.NameHandling.values())
                    .withInitialValue(metadata.getIntegritySettings().nameHandling)
                    .create(x, y, width, height, translatable("chesttracker.gui.editMemoryBank.integrity.nameHandling"));
        });
        integrity.add((x, y, width, height) -> {
            return CycleButton.onOffBuilder(metadata.getIntegritySettings().checkPeriodicallyForMissingBlocks)
                    .create(x, y, width, height, translatable("chesttracker.gui.editMemoryBank.integrity.periodicCheck"));
        });
        integrity.add((x, y, width, height) -> {
            return CycleButton.onOffBuilder(metadata.getIntegritySettings().checkPeriodicallyForMissingBlocks)
                    .create(x, y, width, height, translatable("chesttracker.gui.editMemoryBank.integrity.periodicCheck"));
        });

        addColumn(translatable("chesttracker.gui.editMemoryBank.integrity"), integrity, 0);*/

        addBottomButtons(bottomButtons);

        setupSettings(this.menuHeight - SETTINGS_TOP - 2 - (MARGIN + BUTTON_HEIGHT) * bottomButtons.size());
    }

    private void setupSettings(int height) {
        settingsTabSelector = this.addRenderableWidget(new StringSelectorWidget<>(this.left + MARGIN,
                this.top + SETTINGS_TOP,
                SETTINGS_TAB_SELECTOR_WIDTH,
                height,
                CommonComponents.EMPTY,
                this::setSettingsTab));
        var selectorOptions = new LinkedHashMap<SettingsTab, Component>();
        selectorOptions.put(SettingsTab.INTEGRITY, Component.translatable("chesttracker.gui.editMemoryBank.integrity"));
        selectorOptions.put(SettingsTab.EMPTY, Component.literal("Empty"));

        settingsTabSelector.setOptions(selectorOptions);
        settingsTabSelector.setHighlight(SettingsTab.INTEGRITY);

        addSetting(this.addRenderableWidget(CycleButton.onOffBuilder(metadata.getIntegritySettings().removeOnPlayerBlockBreak)
                .create(getSettingsX(0),
                        getSettingsY(1),
                        getSettingsWidth(1),
                        BUTTON_HEIGHT,
                        translatable("chesttracker.gui.editMemoryBank.integrity.blockBreak"),
                        (cycleButton, newValue) -> metadata.getIntegritySettings().removeOnPlayerBlockBreak = newValue
                )), SettingsTab.INTEGRITY);

        addSetting(this.addRenderableWidget(CycleButton.onOffBuilder(metadata.getIntegritySettings().checkPeriodicallyForMissingBlocks)
                .create(getSettingsX(1),
                        getSettingsY(1),
                        getSettingsWidth(1),
                        BUTTON_HEIGHT,
                        translatable("chesttracker.gui.editMemoryBank.integrity.periodicCheck"),
                        (cycleButton, newValue) -> metadata.getIntegritySettings().checkPeriodicallyForMissingBlocks = newValue
                )), SettingsTab.INTEGRITY);

        addSetting(this.addRenderableWidget(CycleButton.builder(Metadata.IntegritySettings.NameHandling::getLabel)
                .withValues(Metadata.IntegritySettings.NameHandling.values())
                .withInitialValue(metadata.getIntegritySettings().nameHandling)
                .create(getSettingsX(0),
                        getSettingsY(2),
                        getSettingsWidth(1),
                        BUTTON_HEIGHT,
                        translatable("chesttracker.gui.editMemoryBank.integrity.nameHandling"),
                        (cycleButton, newValue) -> metadata.getIntegritySettings().nameHandling = newValue
                )), SettingsTab.INTEGRITY);

        setSettingsTab(SettingsTab.INTEGRITY);
    }

    private void addSetting(AbstractWidget widget, SettingsTab tab) {
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
        final int settingsAreaWidth = this.menuWidth - BUTTON_MARGIN - SETTINGS_TAB_SELECTOR_WIDTH - 2 * MARGIN;
        //noinspection PointlessArithmeticExpression
        return (settingsAreaWidth - BUTTON_MARGIN * (SETTINGS_MAX_COLUMNS - 1)) / SETTINGS_MAX_COLUMNS;
    }

    private int getSettingsX(int column) {
        final int columnWidth = getSingleSettingsColumnWidth();

        final int baseX = this.left + MARGIN + SETTINGS_TAB_SELECTOR_WIDTH + BUTTON_MARGIN;
        return baseX + column * (columnWidth + BUTTON_MARGIN);
    }

    private int getSettingsY(int row) {
        final int baseY = this.top + SETTINGS_TOP;
        return baseY + row * (BUTTON_HEIGHT + BUTTON_MARGIN);
    }

    private int getSettingsWidth(int columnsTaken) {
        final int columnWidth = getSingleSettingsColumnWidth();

        return columnWidth * columnsTaken + MARGIN * (columnsTaken - 1);
    }

    //
    private void addBottomButtons(List<List<RenderableThingGetter<?>>> buttons) {
        final int rowWidth = this.menuWidth - 2 * MARGIN;
        final int startX = this.left + MARGIN;
        final int startY = this.top + this.menuHeight - MARGIN - BUTTON_HEIGHT;
        final int yOffset = BUTTON_HEIGHT + BUTTON_MARGIN;

        // bottom upwards
        for (int i = 0; i < buttons.size(); i++) {
            var row = buttons.get(i);
            int buttonWidth = (rowWidth - (BUTTON_MARGIN * (row.size() - 1))) / row.size();
            for (int buttonIndex = 0; buttonIndex < row.size(); buttonIndex++) {
                this.addRenderableWidget(row.get(buttonIndex)
                        .get(startX + buttonIndex * (buttonWidth + BUTTON_MARGIN), startY - i * yOffset, buttonWidth, BUTTON_HEIGHT));
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

    // we use YACL here because I'm lazy
    private void openDeleteKeys(Button button) {
        MemoryBank bank = isCurrentIdLoaded() ? MemoryBank.INSTANCE : StorageUtil.getStorage().load(memoryBankId);
        if (bank == null) return;

        var category = ConfigCategory.createBuilder()
                .name(translatable("chesttracker.gui.editMemoryBank.manageKeys"));

        for (ResourceLocation key : bank.getKeys()) {
            category.option(ButtonOption.createBuilder()
                    .name(Component.literal(key.toString()))
                    .text(translatable("selectServer.delete"))
                    .description(OptionDescription.of(
                            translatable("chesttracker.gui.editMemoryBank.deleteKey.description", key),
                            CommonComponents.NEW_LINE,
                            translatable("chesttracker.config.irreversable").withStyle(ChatFormatting.RED)
                    )).action((screen, option) -> {
                        bank.removeKey(key);
                        option.setAvailable(false);
                        if (isCurrentIdLoaded()) {
                            MemoryBank.save();
                        } else {
                            bank.setId(memoryBankId);
                            StorageUtil.getStorage().save(bank);
                        }
                    })
                    .build());
        }

        Minecraft.getInstance().setScreen(YetAnotherConfigLib.createBuilder()
                .title(translatable("chesttracker.gui.editMemoryBank.manageKeys"))
                .category(category.build())
                .build()
                .generateScreen(this));

    }

    // Create and load a memory bank using the current load context, then run the load callback.
    private void loadOrCreate(Button button) {
        MemoryBank.loadOrCreate(memoryBankId, metadata);
        afterBankLoaded.run();
    }

    // Create and load a memory bank using the current load context, then run the load callback.
    private void createDefault(LoadContext ctx) {
        MemoryBank.loadOrCreate(ctx.connectionId(), Metadata.from(ctx.name()));
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
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        NinePatcher.BACKGROUND.draw(graphics, this.left, this.top, this.menuWidth, this.menuHeight);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(Minecraft.getInstance().font, this.title, left + MARGIN, this.top + MARGIN, TextColours.getLabelColour(), false);
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
        INTEGRITY,
        EMPTY
    }
}
