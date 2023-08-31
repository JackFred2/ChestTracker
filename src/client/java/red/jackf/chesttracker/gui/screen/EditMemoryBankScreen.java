package red.jackf.chesttracker.gui.screen;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
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
import red.jackf.chesttracker.storage.StorageUtil;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.StringUtil;

import java.time.Instant;
import java.util.HashMap;

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
        super(Component.translatable("chesttracker.gui.editMemoryBank." + (memoryBankId == null ? "create" : "edit")));
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
                b -> this.onClose())).setTooltip(Tooltip.create(Component.translatable("mco.selectServer.close")));

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
        var idLabel = Component.translatable("chesttracker.gui.editMemoryBank.id");
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
        var nameLabel = Component.translatable("mco.backup.entry.name");
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
        int saveLoadButtonsHeight = this.top + this.menuHeight - MARGIN - BUTTON_HEIGHT;
        int fullWidth = this.menuWidth - 2 * MARGIN;
        int halfWidth = (fullWidth - BUTTON_MARGIN) / 2;
        int halfXPos = left + halfWidth + MARGIN + BUTTON_MARGIN;

        // delete everything
        if (StorageUtil.getStorage().getAllIds().contains(memoryBankId)) {
            saveLoadButtonsHeight -= (BUTTON_HEIGHT + BUTTON_MARGIN);
            this.addRenderableWidget(new HoldToConfirmButton(this.left + MARGIN,
                    this.top + this.menuHeight - (MARGIN + BUTTON_HEIGHT),
                    halfWidth,
                    BUTTON_HEIGHT,
                    Component.translatable("selectServer.deleteButton"),
                    Constants.ARE_YOU_REALLY_SURE_BUTTON_HOLD_TIME,
                    this::delete));

            this.addRenderableWidget(Button.builder(Component.translatable("chesttracker.gui.editMemoryBank.manageKeys"), this::openDeleteKeys)
                    .bounds(halfXPos, this.top + this.menuHeight - (MARGIN + BUTTON_HEIGHT), halfWidth, BUTTON_HEIGHT)
                    .build());
        }

        int saveLoadButtonWidth = fullWidth;

        // load
        if (inGame && !isCurrentIdLoaded()) {
            saveLoadButtonWidth = halfWidth;
            this.addRenderableWidget(Button.builder(Component.translatable(isCreatingNewBank ? "mco.create.world" : "structure_block.mode.load"), this::load)
                    .bounds(halfXPos,
                            saveLoadButtonsHeight,
                            saveLoadButtonWidth,
                            BUTTON_HEIGHT)
                    .build());
        }

        // save
        this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.save"), this::save)
                .bounds(left + MARGIN,
                        saveLoadButtonsHeight,
                        saveLoadButtonWidth,
                        BUTTON_HEIGHT)
                .build());
    }

    // we use YACL here because I'm lazy
    private void openDeleteKeys(Button button) {
        MemoryBank bank = isCurrentIdLoaded() ? MemoryBank.INSTANCE : StorageUtil.getStorage().load(memoryBankId);
        if (bank == null) return;

        var category = ConfigCategory.createBuilder()
                .name(Component.translatable("chesttracker.gui.editMemoryBank.manageKeys"));

        for (ResourceLocation key : bank.getKeys()) {
            category.option(ButtonOption.createBuilder()
                    .name(Component.literal(key.toString()))
                    .text(Component.translatable("selectServer.delete"))
                    .description(OptionDescription.of(
                            Component.translatable("chesttracker.gui.editMemoryBank.deleteKey.description", key),
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
                .title(Component.translatable("chesttracker.gui.editMemoryBank.manageKeys"))
                .category(category.build())
                .build()
                .generateScreen(this));

    }

    private void load(Button button) {
        if (!isCurrentIdLoaded()) {
            MemoryBank.loadOrCreate(memoryBankId, metadata);
        }
        afterBankLoaded.run();
    }

    private boolean isCurrentIdLoaded() {
        return MemoryBank.INSTANCE != null && MemoryBank.INSTANCE.getId().equals(memoryBankId);
    }

    private void delete(HoldToConfirmButton button) {
        if (isCurrentIdLoaded()) MemoryBank.unload();
        StorageUtil.getStorage().delete(memoryBankId);
        this.onClose();
    }

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
}
