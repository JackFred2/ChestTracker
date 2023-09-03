package red.jackf.chesttracker.gui.screen;

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
import red.jackf.chesttracker.gui.widget.TextWidget;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.memory.Metadata;
import red.jackf.chesttracker.storage.ConnectionSettings;
import red.jackf.chesttracker.storage.LoadContext;
import red.jackf.chesttracker.storage.StorageUtil;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static net.minecraft.network.chat.Component.translatable;

/**
 * Shows a UI for managing an individual memory bank. Possibly the currently loaded bank.
 */
public class EditMemoryBankScreen extends Screen {
    private static final int WIDTH = 264;
    private static final int HEIGHT = 192;
    private static final int MARGIN = 8;
    private static final int BUTTON_MARGIN = 5;
    private static final int CLOSE_BUTTON_SIZE = 12;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ID_TOP = 30;
    private static final int NAME_TOP = 45;
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
            var label = StorageUtil.getStorage().getDescriptionLabel(memoryBankId);
            var width = font.width(label);
            this.addRenderableOnly(new TextWidget(this.left + menuWidth - BUTTON_MARGIN - CLOSE_BUTTON_SIZE - BUTTON_MARGIN - width,
                    top + MARGIN,
                    label,
                    TextColours.getLabelColour()));
        }

        // ID
        var idLabel = translatable("chesttracker.gui.editMemoryBank.id");
        this.addRenderableOnly(new TextWidget(this.left + MARGIN,
                this.top + ID_TOP,
                idLabel,
                TextColours.getLabelColour()));
        var bankIdText = Component.literal(memoryBankId);
        if (ChestTrackerConfig.INSTANCE.getConfig().gui.hideMemoryIds) bankIdText = bankIdText.withStyle(ChatFormatting.OBFUSCATED);
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

        // bottom buttons
        List<List<RenderableThingGetter<?>>> bottomButtons = new ArrayList<>();

        //var markDefaultButton = this.addRenderableWidget(Button.builder(translatable("chesttracker.gui.editMemoryBank.setAsDefault"), this::markDefault).build());

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
                            var defaultButton = Button.builder(translatable("chesttracker.gui.editMemoryBank.alreadyDefault"), b -> {})
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

        addBottomButtons(bottomButtons);
    }

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
                this.addRenderableWidget(row.get(buttonIndex).get(startX + buttonIndex * (buttonWidth + BUTTON_MARGIN), startY - i * yOffset, buttonWidth, BUTTON_HEIGHT));
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
}
