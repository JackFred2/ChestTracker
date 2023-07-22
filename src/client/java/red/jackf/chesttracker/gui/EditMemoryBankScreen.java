package red.jackf.chesttracker.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.gui.util.NinePatcher;
import red.jackf.chesttracker.gui.util.TextColours;
import red.jackf.chesttracker.gui.widget.CustomEditBox;
import red.jackf.chesttracker.gui.widget.HoldToConfirmButton;
import red.jackf.chesttracker.gui.widget.TextWidget;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.storage.LoadContext;
import red.jackf.chesttracker.storage.StorageUtil;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.StringUtil;

import java.time.Instant;
import java.util.HashMap;

/**
 * Shows a UI for managing an individual memory bank. Possibly the currently loaded bank.
 */
public class EditMemoryBankScreen extends Screen {
    private static final int WIDTH = 264;
    private static final int HEIGHT = 192;
    private static final int MARGIN = 8;
    private static final int BUTTON_MARGIN = 5;
    private static final int CONFIRM_BUTTON_SIZE = 12;
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
    private final MemoryBank.Metadata metadata;
    private final boolean isCreatingNewBank;

    protected EditMemoryBankScreen(@Nullable Screen parent, Runnable afterBankLoaded, @Nullable String memoryBankId) {
        super(Component.translatable("chesttracker.gui.editMemoryBank." + (memoryBankId == null ? "create" : "edit")));
        this.parent = parent;
        this.afterBankLoaded = afterBankLoaded;
        this.isCreatingNewBank = memoryBankId == null;
        if (isCreatingNewBank) {
            this.memoryBankId = getNextIdDefault();
            this.metadata = new MemoryBank.Metadata(this.memoryBankId.substring("user/".length()), Instant.now());
        } else {
            this.memoryBankId = memoryBankId;
            var metadata = StorageUtil.getStorage().getMetadata(memoryBankId);
            this.metadata = metadata == null ? MemoryBank.Metadata.blank() : metadata;
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
                left + menuWidth - (BUTTON_MARGIN + CONFIRM_BUTTON_SIZE),
                top + BUTTON_MARGIN,
                CONFIRM_BUTTON_SIZE,
                CONFIRM_BUTTON_SIZE,
                0,
                0,
                CONFIRM_BUTTON_SIZE,
                ChestTracker.guiTex("widgets/return_button"),
                CONFIRM_BUTTON_SIZE,
                CONFIRM_BUTTON_SIZE * 3,
                b -> this.onClose())).setTooltip(Tooltip.create(Component.translatable("mco.selectServer.close")));

        // ID
        var idLabel = Component.translatable("chesttracker.gui.editMemoryBank.id");
        this.addRenderableOnly(new TextWidget(this.left + MARGIN,
                this.top + ID_TOP,
                idLabel,
                TextColours.getLabelColour(),
                false));
        this.addRenderableOnly(new TextWidget(this.left + MARGIN + font.width(idLabel) + 4,
                this.top + ID_TOP,
                Component.literal(memoryBankId),
                TextColours.getLabelColour(),
                true));

        // Name
        var nameLabel = Component.translatable("mco.backup.entry.name");
        this.addRenderableOnly(new TextWidget(this.left + MARGIN,
                this.top + NAME_TOP,
                nameLabel,
                TextColours.getLabelColour(),
                false));
        this.nameEditBox = this.addRenderableWidget(new CustomEditBox(font,
                this.left + MARGIN + font.width(nameLabel) + 4,
                this.top + NAME_TOP - 2,
                menuWidth - 2 * MARGIN - font.width(nameLabel) - 4,
                font.lineHeight + 3,
                this.nameEditBox,
                CommonComponents.EMPTY));
        this.nameEditBox.setResponder(metadata::setName);
        this.nameEditBox.setValue(metadata.getName() != null ? metadata.getName() : memoryBankId);

        int saveLoadButtonsHeight = this.top + this.menuHeight - MARGIN - BUTTON_HEIGHT;

        // delete
        if (StorageUtil.getStorage().getAllIds().contains(memoryBankId)) {
            saveLoadButtonsHeight -= (BUTTON_HEIGHT + BUTTON_MARGIN);
            this.addRenderableWidget(new HoldToConfirmButton(this.left + MARGIN,
                    this.top + this.menuHeight - (MARGIN + BUTTON_HEIGHT),
                    this.menuWidth - 2 * MARGIN,
                    BUTTON_HEIGHT,
                    Component.translatable("selectServer.deleteButton"),
                    Constants.ARE_YOU_REALLY_SURE_BUTTON_HOLD_TIME,
                    this::delete));
        }

        int saveLoadButtonWidth = this.menuWidth - 2 * MARGIN;

        // load
        if (inGame && !isCurrentIdLoaded()) {
            saveLoadButtonWidth = (saveLoadButtonWidth - BUTTON_MARGIN) / 2;
            this.addRenderableWidget(Button.builder(Component.translatable(isCreatingNewBank ? "mco.create.world" : "structure_block.mode.load"), this::load)
                    .bounds(left + saveLoadButtonWidth + MARGIN + BUTTON_MARGIN,
                            saveLoadButtonsHeight,
                            saveLoadButtonWidth,
                            BUTTON_HEIGHT)
                    .build());
        }

        // save
        this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.save"), this::finish)
                .bounds(left + MARGIN,
                        saveLoadButtonsHeight,
                        saveLoadButtonWidth,
                        BUTTON_HEIGHT)
                .build());
    }

    private void load(Button button) {
        if (!isCurrentIdLoaded()) {
            var ctx = LoadContext.get(Minecraft.getInstance());
            if (ctx != null)
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

    private void finish(Button button) {
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
