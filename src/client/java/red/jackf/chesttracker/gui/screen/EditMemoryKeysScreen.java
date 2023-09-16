package red.jackf.chesttracker.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import red.jackf.chesttracker.gui.GuiConstants;
import red.jackf.chesttracker.gui.widget.DragHandleWidget;
import red.jackf.chesttracker.gui.widget.HoldToConfirmButton;
import red.jackf.chesttracker.gui.widget.ItemButton;
import red.jackf.chesttracker.memory.LightweightStack;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.storage.Storage;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.network.chat.Component.translatable;

public class EditMemoryKeysScreen extends BaseUtilScreen {
    private static final int MAX_WIDTH = 480;
    private static final int CONTENT_TOP = 30;
    private static final int DELETE_BUTTON_SIZE = 60;
    private static final int NAME_BOX_MARGIN = 1;
    private final Screen parent;
    private final MemoryBank bank;
    private final Map<ResourceLocation, EditBox> editBoxes = new HashMap<>();
    private final Map<ResourceLocation, DragHandleWidget> dragHandles = new HashMap<>();

    private boolean firstLoad = false;
    private boolean scheduleRebuild = false;

    protected EditMemoryKeysScreen(Screen parent, MemoryBank memoryBank) {
        super(translatable("chesttracker.gui.editMemoryKeys"));
        this.parent = parent;
        this.bank = memoryBank;
    }

    @Override
    public void tick() {
        if (this.scheduleRebuild) {
            this.rebuildWidgets();
            this.scheduleRebuild = false;
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    protected void init() {
        super.init();

        this.dragHandles.clear();

        //this.menuWidth = Mth.clamp(this.width, GuiConstants.UTIL_GUI_WIDTH, MAX_WIDTH);
        this.left = (this.width - this.menuWidth) / 2;

        var font = Minecraft.getInstance().font;
        final int workingWidth = this.menuWidth - 2 * GuiConstants.MARGIN;
        final int spacing = GuiConstants.SMALL_MARGIN;
        final int startY = this.top + CONTENT_TOP;

        for (var index = 0; index < bank.getKeys().size(); index++) {
            var key = bank.getKeys().get(index);
            int x = this.left + GuiConstants.MARGIN;
            int y = startY + index * (ItemButton.SIZE + spacing);

            // drag handle
            int currentIndex = index;
            this.dragHandles.put(key, this.addRenderableWidget(new DragHandleWidget(x,
                    y,
                    x,
                    startY - Mth.positiveCeilDiv(spacing, 2),
                    workingWidth,
                    ItemButton.SIZE + spacing,
                    0,
                    bank.getKeys().size(),
                    newIndex -> {
                        if (newIndex < currentIndex) {
                            this.bank.getMetadata().moveIcon(currentIndex, newIndex);
                            scheduleRebuild = true;
                        } else if (newIndex > currentIndex + 1) {
                            this.bank.getMetadata().moveIcon(currentIndex, newIndex - 1);
                            scheduleRebuild = true;
                        }
                    })));

            x += DragHandleWidget.WIDTH + spacing;

            // icon
            this.addRenderableWidget(new ItemButton(
                            bank.getMetadata().getOrCreateIcon(key).toStack(),
                            x,
                            y,
                            button -> Minecraft.getInstance().setScreen(new SelectorScreen<>(
                                    translatable("chesttracker.gui.editMemoryKeys.setIcon"),
                                    this,
                                    GuiConstants.DEFAULT_ICON_ORDER,
                                    i -> {
                                        if (i != null) {
                                            this.bank.getMetadata().setIcon(key, new LightweightStack(i));
                                            scheduleRebuild = true;
                                        }
                                    }
                            )),
                            ItemButton.Background.VANILLA))
                    .setTooltip(Tooltip.create(translatable("chesttracker.gui.editMemoryKeys.setIcon")));

            x += ItemButton.SIZE + spacing;

            // id label/edit box
            var nameEditBox = this.addRenderableWidget(new EditBox(
                    font,
                    x,
                    y + NAME_BOX_MARGIN,
                    workingWidth - ItemButton.SIZE - 3 * spacing - DELETE_BUTTON_SIZE - 2 * NAME_BOX_MARGIN - DragHandleWidget.WIDTH,
                    ItemButton.SIZE - 2 * NAME_BOX_MARGIN,
                    this.editBoxes.get(key),
                    translatable("chesttracker.gui.editMemoryKeys.hint")));
            if (!firstLoad) nameEditBox.setValue(key.toString());
            nameEditBox.setHint(translatable("chesttracker.gui.editMemoryKeys.hint"));
            this.editBoxes.put(key, nameEditBox);

            x += nameEditBox.getWidth() + spacing;

            // delete
            this.addRenderableWidget(new HoldToConfirmButton(
                    x,
                    y,
                    DELETE_BUTTON_SIZE,
                    20,
                    translatable("selectServer.deleteButton"),
                    GuiConstants.ARE_YOU_SURE_BUTTON_HOLD_TIME,
                    button -> {
                        this.bank.removeKey(key);
                        Storage.save(this.bank);
                        // can't schedule rebuilt during rendering because CME, so do it on tick
                        this.scheduleRebuild = true;
                    }));
        }

        // save
        this.addRenderableWidget(Button.builder(translatable("selectWorld.edit.save"), b -> {
                    Storage.save(this.bank);
                    onClose();
                }).bounds(
                        this.left + GuiConstants.MARGIN,
                        this.top + this.menuHeight - GuiConstants.MARGIN - 20,
                        workingWidth,
                        20)
                .build());

        this.firstLoad = true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean anyTrue = false;
        for (DragHandleWidget widget : this.dragHandles.values()) {
            anyTrue |= widget.mouseReleased(mouseX, mouseY, button);
        }
        if (anyTrue) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
