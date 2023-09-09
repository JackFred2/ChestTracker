package red.jackf.chesttracker.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.storage.StorageUtil;

import static net.minecraft.network.chat.Component.translatable;

public class EditMemoryKeyScreen extends BaseUtilScreen {
    private final Screen parent;
    private final String memoryBankId;

    protected EditMemoryKeyScreen(Screen parent, String memoryBankId, boolean isCreating) {
        super(translatable("chesttracker.gui.memoryKeys"));
        this.parent = parent;
        this.memoryBankId = memoryBankId;

        var bank = isCurrentLoaded() ? MemoryBank.INSTANCE : StorageUtil.getStorage().load(memoryBankId);
    }

    private boolean isCurrentLoaded() {
        return MemoryBank.INSTANCE != null && MemoryBank.INSTANCE.getId().equals(memoryBankId);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    protected void init() {
        super.init();
    }
}
