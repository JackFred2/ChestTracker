package red.jackf.chesttracker.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.chesttracker.gui.GuiConstants;
import red.jackf.chesttracker.gui.widget.HoldToConfirmButton;
import red.jackf.chesttracker.gui.widget.ItemButton;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.storage.StorageUtil;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.network.chat.Component.translatable;

public class EditMemoryKeysScreen extends BaseUtilScreen {
    private static final int CONTENT_TOP = 30;
    private static final int DELETE_BUTTON_SIZE = 100;
    private final Screen parent;
    private final String memoryBankId;
    private final MemoryBank bank;
    private final Map<ResourceLocation, EditBox> editBoxes = new HashMap<>();

    private boolean firstLoad = false;
    private boolean scheduleRebuild = false;

    protected EditMemoryKeysScreen(Screen parent, String memoryBankId) {
        super(translatable("chesttracker.gui.editMemoryKeys"));
        this.parent = parent;
        this.memoryBankId = memoryBankId;
        this.bank = isCurrentLoaded() ? MemoryBank.INSTANCE : StorageUtil.getStorage().load(memoryBankId);
    }

    private boolean isCurrentLoaded() {
        return MemoryBank.INSTANCE != null && MemoryBank.INSTANCE.getId().equals(memoryBankId);
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

        var font = Minecraft.getInstance().font;
        int startX = this.left + GuiConstants.MARGIN;
        int workingArea = this.menuWidth - 2 * GuiConstants.MARGIN;

        for (var index = 0; index < bank.getKeys().size(); index++) {
            var key = bank.getKeys().get(index);
            int y = this.top + CONTENT_TOP + index * (ItemButton.SIZE + GuiConstants.SMALL_MARGIN);

            // icon
            this.addRenderableWidget(new ItemButton(
                    new ItemStack(Items.CRAFTING_TABLE),
                    startX,
                    y,
                    translatable("chesttracker.gui.editMemoryKeys.setIcon"),
                    button -> {},
                    ItemButton.Background.VANILLA,
                    0));

            // id label/edit box
            var nameEditBox = this.addRenderableWidget(new EditBox(
                    font,
                    startX + ItemButton.SIZE + GuiConstants.SMALL_MARGIN,
                    y,
                    workingArea - ItemButton.SIZE - 2 * GuiConstants.SMALL_MARGIN - DELETE_BUTTON_SIZE,
                    ItemButton.SIZE,
                    this.editBoxes.get(key),
                    translatable("chesttracker.gui.editMemoryKeys.hint")));
            if (!firstLoad) nameEditBox.setValue(key.toString());
            nameEditBox.setHint(translatable("chesttracker.gui.editMemoryKeys.hint"));
            this.editBoxes.put(key, nameEditBox);

            // delete
            this.addRenderableWidget(new HoldToConfirmButton(
                    startX + workingArea - DELETE_BUTTON_SIZE,
                    y,
                    100,
                    20,
                    translatable("selectServer.deleteButton"),
                    GuiConstants.ARE_YOU_SURE_BUTTON_HOLD_TIME,
                    button -> {
                        this.bank.removeKey(key);
                        if (isCurrentLoaded()) {
                            MemoryBank.save();
                        } else {
                            StorageUtil.getStorage().save(bank);
                        }
                        // can't schedule rebuilt during rendering because CME, so do it on tick
                        this.scheduleRebuild = true;
                    }));
        }
        this.firstLoad = true;
    }
}
